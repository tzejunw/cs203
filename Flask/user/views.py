from . import user
from user.forms import LoginForm, RegisterForm, UpdateAccountForm, LoginOTPForm

from flask import Flask, abort, jsonify, make_response, render_template, request, redirect, url_for, flash
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

        response = requests.post(
            "http://localhost:8080/user/create", 
            json=data,
            headers={"Content-Type": "application/json"}
        )

        response_text = ""
        if response.status_code == 200:
            try:
                response_text = response.json()
                return redirect(url_for('user.login'))
            except ValueError:
                response_text = response.text
                print("Received a non-JSON response from the server.")
            flash(response_text, 'success')
            return redirect(url_for('user.login'))
        else:
            response_text = response.text
            print(f"Failed to register user. Status code: {response.status_code}")
        print(response_text)
        flash(response_text, 'danger')
        
    return render_template('user/register.html', form=form)

# /user/login
@user.route('/login', methods=['GET', 'POST'])
def login():
    form = LoginForm();
    if request.method == 'POST' and form.validate_on_submit():
        # form.email.errors = ["User is not valid"]
        data = {
            "email": form.email.data,
            "password": form.password.data
        }

        response = requests.post(
            "http://localhost:8080/user/login", 
            json=data,
            headers={"Content-Type": "application/json"}
        )
        
        if response.status_code == 200:
            token = response.text
            flash("Login Successfully!", 'success')
            response = make_response(redirect(url_for('frontend.index')))
            response.set_cookie('jwt', token, max_age=60*60)
            return response
        else:
            flash(response.text, 'error')
            print(f"Failed to login. Status code: {response.status_code}")

        
    return render_template('user/login.html', form=form)

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

    data = {
        "username": ""
    }
    
    if request.method == 'GET':
        # form.email.errors = ["User is not valid"]
        response = requests.get(
            "http://localhost:8080/user/get", 
            headers = {
                "Authorization": f"Bearer {jwt_cookie}",
                "Content-Type": "application/json"
            }
        )

        if response.status_code == 200:
            data = response.json()
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
            "userName": data.get('userName'), # username nt getting from data
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
            flash(response, 'error')
            print(response)
        
    return render_template('user/update_account.html', form=form, userDetails=data)

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
        abort(response.status_code)
    
    

@user.route('/<string:id>/', methods=['GET', 'POST'])
def view_player_profile(id):
    return render_template('user/view_player_profile.html')