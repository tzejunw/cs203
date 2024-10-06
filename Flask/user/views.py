from . import user
from user.forms import LoginForm, RegisterForm, UpdateAccountForm, UpdatePasswordForm, LoginOTPForm

from flask import Flask, abort, jsonify, make_response, render_template, request, redirect, url_for, flash, current_app
from flask_wtf import Form, FlaskForm 
from wtforms import ValidationError, StringField, PasswordField, SubmitField, \
    TextAreaField, BooleanField, RadioField, FileField, DateField, SelectField
from wtforms.validators import DataRequired, Length, EqualTo, Email, URL, AnyOf, Optional
from werkzeug.security import generate_password_hash 
import requests
import json
from datetime import datetime, date

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
            print(error["debug"])
    except ValueError: # For errors in pure text
        flash(response.text, 'danger')
    print(f"Error Status code: {response.status_code}.")


# /user/register
@user.route('/register', methods=['GET', 'POST'])
def register():
    form = RegisterForm();
    if request.method == 'POST' and form.validate_on_submit():
        # form.email.errors = ["Email is not valid"]
        # form.name.errors = ["Name is not valid"]
        
        birthday_str = YmdToDmyConverter(form.birthday.data.strftime('%Y-%m-%d'))
        
        data = {
            "userName": form.userName.data,
            "name": form.name.data,
            "birthday": birthday_str,
            "email": form.email.data,
            "gender": form.gender.data,
            "password": form.password.data
        }
        header={"Content-Type": "application/json"}

        response = {}
        try:
            response = requests.post(current_app.config['BACKEND_URL'] + "/user/create", json=data, headers=header)
        except Exception as e: 
            flash("Sorry, we are unable to connect to the server right now, please try again later.", "danger")
            print(e)
            return render_template('user/register.html', form=form)

        if response.status_code == 200:
            flash(handleNormalResponses(response), 'success')
            return redirect(url_for('user.login'))
        else:
            handleErrorResponses(response)
        
    return render_template('user/register.html', form=form)

# /user/login
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
            response = requests.post(current_app.config['BACKEND_URL'] + "/user/login", json=data, headers=header)
        except Exception as e: 
            flash("Sorry, we are unable to connect to the server right now, please try again later.", "danger")
            flash("Ensure Spring Boot server is running, and has no problems on it.", "info")
            print(e)
            return render_template('user/login.html', form=form)
        
        if response.status_code == 200:
            token = response.text
            response = make_response(redirect(url_for('index')))

            getUserDetails = requests.get(
                current_app.config['BACKEND_URL'] + "/user/get", 
                headers = {
                    "Authorization": f"Bearer {token}",
                    "Content-Type": "application/json"
                }
            )

            if getUserDetails.status_code == 200:
                data = getUserDetails.json()
                if data.get('userName'):
                    response.set_cookie('userName', data.get('userName'), max_age=60*60)
            
            flash("Login Successfully!", 'success')
            response.set_cookie('jwt', token, max_age=60*60)
            return response
        else:
            handleErrorResponses(response)
        
    return render_template('user/login.html', form=form)

@user.route('/logout')
def logout():
    jwt_cookie = request.cookies.get('jwt')
    header= {
        "Authorization": f"Bearer {jwt_cookie}",
        "Content-Type": "application/json"
    }

    response = {}
    try:
        response = requests.post(current_app.config['BACKEND_URL'] + "/user/logout", headers=header)
    except Exception as e: 
        print(e)
    
    response = make_response(redirect(url_for('user.login')))
    response.set_cookie('jwt', '', expires=0)
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
            current_app.config['BACKEND_URL'] + "/user/get", 
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
            "http://localhost:8080/user/update", 
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
            current_app.config['BACKEND_URL'] + "/user/get", 
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
            "http://localhost:8080/user/updatePassword", 
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
        "http://localhost:8080/user/delete", 
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
