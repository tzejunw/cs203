import os
from . import user
from user.forms import LoginForm, RegisterForm, RegisterAccountForm, RegisterProfileForm, UpdateAccountForm, UpdatePasswordForm, LoginOTPForm

from flask import Flask, abort, jsonify, make_response, render_template, request, redirect, url_for, flash, current_app
from flask_wtf import Form, FlaskForm 
from wtforms import ValidationError, StringField, PasswordField, SubmitField, \
    TextAreaField, BooleanField, RadioField, FileField, DateField, SelectField
from wtforms.validators import DataRequired, Length, EqualTo, Email, URL, AnyOf, Optional
from werkzeug.security import generate_password_hash 
import requests
import json
from datetime import datetime, date

firebase_config = {
    'apiKey': "AIzaSyCDJEkLkIfEoggaGDKY5TJjrkXsUolIJDk",
    'authDomain': "https://cs203-a263b.firebaseapp.com/",
    'projectId': "cs203-a263b",
    'storageBucket': "cs203-a263b.appspot.com",
    'messagingSenderId': "28927577611",
    'appId': "1:28927577611:web:42e3ffc3901ab028e3410e",
    'measurementId': "G-SSRD2ZW3NS",
}

def YmdToDmyConverter(date_str):
    # The backend only accepts DD/MM/YYYY
    birthday_str = datetime.strptime(date_str, '%Y-%m-%d').strftime('%d/%m/%Y')
    return birthday_str

def handleNormalResponses(response):
    try: # For responses in JSON format
        response_text = response.json()
    except ValueError: # For responses in pure text
        response_text = response.text
    return response_text

def handleErrorResponses(response):
    try: # For errors in JSON format
        error = response.json()
        flash(error["message"], 'danger')
        if "debug" in error:
            print("Debug - " + error["debug"])
    except ValueError: # For errors in pure text
        flash(response.text, 'danger')
    print(f"Error Status code: {response.status_code}.")

@user.route('/register', methods=['GET', 'POST'])
def register():
    form = RegisterAccountForm();
    if request.method == 'POST' and form.validate_on_submit():      
        data = {
            "email": form.email.data,
            "password": form.password.data
        }
        header={"Content-Type": "application/json"}

        response = {}
        try:
            response = requests.post(current_app.config['BACKEND_URL'] + "/user", json=data, headers=header)
        except Exception as e: 
            flash("Sorry, we are unable to connect to the server right now, please try again later.", "danger")
            print(e)
            return render_template('user/register_account.html', form=form)

        if response.status_code == 200:
            flash("Account created. Please verify your email to proceed.", 'success')
            return redirect(url_for('user.verify_email'))
        else:
            handleErrorResponses(response)
        
    return render_template('user/register_account.html', form=form, firebase_config=firebase_config)

@user.route('/verify_email')
def verify_email():
    return render_template('user/register_confirm_email.html')

@user.route('/register/profile', methods=['GET', 'POST'])
def register_profile():
    jwt_cookie = request.cookies.get('jwt')
    form = RegisterProfileForm()

    if request.method == 'GET':
        form.name.data = request.cookies.get('name')
    
    if request.method == 'POST' and form.validate_on_submit():      
        birthday_str = YmdToDmyConverter(form.birthday.data.strftime('%Y-%m-%d'))
        
        data = {
            "userName": form.userName.data,
            "name": form.name.data,
            "birthday": birthday_str,
            "gender": form.gender.data
        }
        header= {
            "Content-Type": "application/json",
            "Authorization": f"Bearer {jwt_cookie}"
        }

        response = {}
        try:
            response = requests.post(current_app.config['BACKEND_URL'] + "/user/profile", json=data, headers=header)
        except Exception as e: 
            flash("Sorry, we are unable to connect to the server right now, please try again later.", "danger")
            print(e)
            return render_template('user/register_profile.html', form=form)

        if response.status_code == 200:
            flash(handleNormalResponses(response), 'success')
            response = make_response(redirect(url_for('index')))
            response.set_cookie('userName', form.userName.data, max_age=60*60)
            response.set_cookie('name', '', expires=0)
            response.set_cookie('registration', '', expires=0)
            return response
        else:
            handleErrorResponses(response)
    return render_template('user/register_profile.html', form=form)

# /login
@user.route('/login', methods=['GET', 'POST'])
def login():
    form = LoginForm()

    if request.method == 'GET' and request.cookies.get('jwt'):
        flash("Don't delulu, you are already logged in.", "danger")
        previous_page = request.referrer
        # If no referrer, go to the home page
        if previous_page is None:
            return redirect(url_for('index'))
        return redirect(previous_page)

    if request.method == 'POST' and form.validate_on_submit():
        # form.email.errors = ["User is not valid"]
        data = {
            "email": form.email.data,
            "password": form.password.data
        }
        header= {
            "Content-Type": "application/json"
        }

        response = {}
        try:
            response = requests.post(current_app.config['BACKEND_URL'] + "/login", json=data, headers=header)
        except Exception as e: 
            flash("Sorry, we are unable to connect to the server right now, please try again later.", "danger")
            flash("Ensure Spring Boot server is running, and has no problems on it.", "info")
            print(e)
            return render_template('user/login.html', form=form)
        
        if response.status_code == 200:
            token = response.text

            getUserDetails = requests.get(
                current_app.config['BACKEND_URL'] + "/user", 
                headers = {
                    "Authorization": f"Bearer {token}",
                    "Content-Type": "application/json"
                }
            )

            if getUserDetails.status_code == 200:
                response = make_response(redirect(url_for('index')))
                data = getUserDetails.json()
                if data.get('userName'):
                    response.set_cookie('userName', data.get('userName'), max_age=60*60)
            else:
                print("no user record found")
                response = make_response(redirect(url_for('user.register_profile')))
                response.set_cookie('jwt', token, max_age=60*60)
                response.set_cookie('registration', "not_done", max_age=60*60)
            
            flash("Login Successfully!", 'success')
            response.set_cookie('jwt', token, max_age=60*60)
            return response
        else:
            handleErrorResponses(response)
        
    return render_template('user/login.html', form=form, firebase_config=firebase_config)

@user.route('/google_login', methods=['POST'])
def google_login():
    data = request.get_json()
    token = data.get('token')
    name = data.get('name')

    if token is None:
        return jsonify({'status': 'error', 'message': 'Token is missing'}), 400

    print(current_app.config['BACKEND_URL'])
    getUserDetails = requests.get(
        current_app.config['BACKEND_URL'] + "/user", 
        headers = {
            "Authorization": f"Bearer {token}",
            "Content-Type": "application/json"
        }
    )

    if getUserDetails.status_code == 200:
        user_data = getUserDetails.json()
        print(user_data)
        response = jsonify({'status': 'success', 'message': 'Login Successfully!'})
        response.set_cookie('jwt', token, max_age=60*60)
        response.set_cookie('userName', user_data.get('userName'), max_age=60*60)
    else:
        print("no user record found")
        response = jsonify({'status': 'error', 'message': 'No user record found'})
        response.set_cookie('jwt', token, max_age=60*60)
        response.set_cookie('name', name, max_age=60*60)
        response.set_cookie('registration', "not_done", max_age=60*60)

    return response

    
    
@user.route('/logout')
def logout():
    jwt_cookie = request.cookies.get('jwt')
    header= {
        "Authorization": f"Bearer {jwt_cookie}",
        "Content-Type": "application/json"
    }

    response = {}
    try:
        response = requests.post(current_app.config['BACKEND_URL'] + "/logout", headers=header)
    except Exception as e: 
        print(e)
    
    response = make_response(redirect(url_for('user.login')))
    response.set_cookie('jwt', '', expires=0)
    response.set_cookie('name', '', expires=0)
    response.set_cookie('registration', '', expires=0)
    flash("Logout successfully", 'success')
    return response
    

@user.route('/login_otp', methods=['GET', 'POST'])
def login_otp():
    form = LoginOTPForm();
    if request.method == 'POST' and form.validate_on_submit():
        form.email.errors = ["User is not valid"]
        print(form.email.data)
    return render_template('user/login_otp.html', form=form)

@user.route('/update_account', methods=['GET', 'POST'])
def update_account():
    jwt_cookie = request.cookies.get('jwt')
    form = UpdateAccountForm()
    
    if request.method == 'GET':
        # form.email.errors = ["User is not valid"]
        response = requests.get(
            current_app.config['BACKEND_URL'] + "/user", 
            headers = {
                "Authorization": f"Bearer {jwt_cookie}",
                "Content-Type": "application/json"
            }
        )

        if response.status_code == 200:
            data = response.json()
            form.userName.data = data.get('userName')
            form.userName(disabled=True)
            form.name.data = data.get('name')
            form.gender.data = data.get('gender')
            if data.get('birthday'):
                date_str = datetime.strptime(data.get('birthday'), '%d/%m/%Y').strftime('%Y-%m-%d')
                form.birthday.data = date_str
                print(date_str)
        else:
            abort(response.status_code)

    if request.method == 'POST' and form.validate_on_submit():
        birthday_str = YmdToDmyConverter(form.birthday.data.strftime('%Y-%m-%d'))

        data = {
            "userName": form.userName.data,
            "name": form.name.data,
            "birthday": birthday_str,
            "email": "admin@gmail.com",
            "gender": form.gender.data
        }

        response = requests.put(
            f"{current_app.config['BACKEND_URL']}/user", 
            json=data,
            headers = {
                "Authorization": f"Bearer {jwt_cookie}",
                "Content-Type": "application/json"
            }
        )

        if response.status_code == 200:
            flash("Successfully updated profile!", 'success')
        else:
            handleErrorResponses(response)
        
    return render_template('user/update_account.html', form=form)

@user.route('/update_password', methods=['GET', 'POST'])
def update_password():
    jwt_cookie = request.cookies.get('jwt')
    form = UpdatePasswordForm()

    if request.method == 'GET':
        # form.email.errors = ["User is not valid"]
        response = requests.get(
            current_app.config['BACKEND_URL'] + "/user", 
            headers = {
                "Authorization": f"Bearer {jwt_cookie}",
                "Content-Type": "application/json"
            }
        )

        if response.status_code != 200:
            abort(response.status_code)
    
    if request.method == 'POST' and form.validate_on_submit():
        data = {
            "password": form.password.data,
        }

        response = requests.put(
            f"{current_app.config['BACKEND_URL']}/user/password", 
            json=data,
            headers = {
                "Authorization": f"Bearer {jwt_cookie}",
                "Content-Type": "application/json"
            }
        )

        if response.status_code == 200:
            response = make_response(redirect(url_for('user.login')))
            response.set_cookie('jwt', '', expires=0)
            flash("Successfully updated password!", 'success')
            flash("For security reasons, we have logged you out of all devices. Please log in again.", 'info')
            return response
        else:
            handleErrorResponses(response)

    return render_template('user/update_password.html', form=form)

# DELETE method is somehow (device) restricted in HTML, so lets just use POST.
@user.route('/delete_user', methods=['POST'])
def delete_user():
    jwt_cookie = request.cookies.get('jwt')
    response = requests.delete(
        f"{current_app.config['BACKEND_URL']}/user", 
        headers = {
            "Authorization": f"Bearer {jwt_cookie}",
            "Content-Type": "application/json"
        }
    )

    if response.status_code == 200:
        flash("Account succesfully deleted.", 'success')
        response = make_response(redirect(url_for('user.login')))
        response.set_cookie('jwt', '', expires=0)
        return response
    else:
        handleErrorResponses(response)
        abort(response.status_code)
    
    

@user.route('/<string:id>/', methods=['GET', 'POST'])
def view_player_profile(id):
    return render_template('user/view_player_profile.html')


@user.route('/manage_tournament/<string:tournament_name>')
def manage_tournament(tournament_name):
    jwt_cookie = request.cookies.get('jwt')  # Retrieve the JWT token
    headers = {
        'Authorization': f'Bearer {jwt_cookie}',  # Add the JWT token to the header
    }

    # First, fetch the tournament details
    tournament_api_url = f'{current_app.config['BACKEND_URL']}/tournament/get?tournamentName={tournament_name}'
    tournament_response = requests.get(tournament_api_url, headers=headers)

    if tournament_response.status_code != 200:
        flash("Error going to tournament management page: " + tournament_response.text, "danger")
        return redirect(url_for('admin.view_tournaments'))

    tournament = tournament_response.json()

    currentUser_api_url =  f'{current_app.config['BACKEND_URL']}/user'
    currentUser_response = requests.get(currentUser_api_url, headers=headers)
    currentUser = currentUser_response.json().get('userName')


    # Fetch the current round name
    round_name = tournament.get('currentRound')  # Retrieve the current round

    if tournament.get('currentRound') == str(tournament.get('expectedNumRounds')):
        round_name = str(int(round_name) + 1)

    print(tournament_name, round_name)

    # Initialize round_data
    round_data = None

    # Only fetch round data if round_name is valid
    if round_name is not None:
        # Update the round API URL to include the JWT token in the headers
        round_api_url = f'{current_app.config['BACKEND_URL']}/tournament/round/get?tournamentName={tournament_name}&roundName={round_name}'
        round_headers = {
            'Authorization': f'Bearer {jwt_cookie}'  # Include the JWT token in the headers
        }
        round_response = requests.get(round_api_url, headers=round_headers)  # Pass the headers

        if round_response.status_code == 200:
            # Check if the response content is empty
            if round_response.text.strip() == "":  # Check for empty response body
                round_data = None
            else:
                try:
                    round_data = round_response.json()  # Attempt to parse JSON
                    if round_data is None:
                        flash("Round data is null.", "warning")
                    elif not isinstance(round_data, dict):  # Adjust based on expected structure
                        flash("Invalid round data received.", "warning")
                        round_data = None  # Set round_data to None
                except requests.exceptions.JSONDecodeError:
                    flash("Error decoding round data response: Invalid JSON", "danger")
        else:
            flash("Error fetching round data: " + round_response.text, "danger")
            print('####' + round_response.text)


     # Fetch standings for the previous round if round_name is valid
    standings_data = None
    round_name_str = str(int(round_name) - 1) if round_name and round_name.isdigit() else None
    if round_name_str is not None:
        standings_api_url = f'{current_app.config['BACKEND_URL']}/tournament/round/standing/get/all?tournamentName={tournament_name}&roundName={round_name_str}'
        standings_response = requests.get(standings_api_url, headers=round_headers)  # Use the same headers

        if standings_response.status_code == 200:
            standings_data = standings_response.json()
            print("STANDINGS DATA: ")
            print(standings_data)
        else:
            flash("Error fetching standings data: " + standings_response.text, "danger")
    
    # round_over = round_data.get('over') if round_data else None

   
    # Render the page with tournament, round, and standings data
    return render_template(
        'user/manage_tournament.html', 
        tournament=tournament, 
        round=round_data,  # round_data may be None if no round is available
        currentUser=currentUser,
        standings=standings_data,  # Pass the standings data to the template
        current_round=tournament.get('currentRound')
       # round_over=round_over # Pass in over to the template as well 
    )
