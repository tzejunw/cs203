from . import user
from user.forms import LoginForm, RegisterForm, UpdateAccountForm, LoginOTPForm

from flask import Flask, render_template, request, redirect, url_for
from flask_wtf import Form, FlaskForm 
from wtforms import ValidationError, StringField, PasswordField, SubmitField, \
    TextAreaField, BooleanField, RadioField, FileField, DateField, SelectField
from wtforms.validators import DataRequired, Length, EqualTo, Email, URL, AnyOf, Optional
from werkzeug.security import generate_password_hash 
import requests
import json

# /user/register
@user.route('/register', methods=['GET', 'POST'])
def register():
    form = RegisterForm();
    if request.method == 'POST' and form.validate_on_submit():
        # form.email.errors = ["Email is not valid"]
        # form.name.errors = ["Name is not valid"]

        data = {
            "userName": form.userName.data,
            "name": form.name.data,
            "birthday": "10/10/2010",
            "email": form.email.data,
            "gender": form.gender.data,
            "password": form.password.data
        }

        response = requests.post(
            "http://localhost:8080/user/create", 
            json=data,
            headers={"Content-Type": "application/json"}
        )
        # response_dict = json.loads(response.json())
        if response.status_code == 200:
            try:
                response_data = response.json()
                print(response_data)
                return redirect(url_for('user.login'))
            except ValueError:
                print("Received a non-JSON response from the server.")
        else:
            print(f"Failed to register user. Status code: {response.status_code}")
        return redirect(url_for('user.login'))
        
    return render_template('user/register.html', form=form)

# /user/login
@user.route('/login', methods=['GET', 'POST'])
def login():
    form = LoginForm();
    if request.method == 'POST' and form.validate_on_submit():
        # form.email.errors = ["User is not valid"]
        print(form.email.data)
    return render_template('user/login.html', form=form)

@user.route('/login_otp', methods=['GET', 'POST'])
def login_otp():
    form = LoginOTPForm();
    if request.method == 'POST' and form.validate_on_submit():
        form.email.errors = ["User is not valid"]
        print(form.email.data)
    return render_template('user/login_otp.html', form=form)

@user.route('/update_account', methods=['GET', 'PUT'])
def update_account():
    ### Sample Data ###
    json_data = '{"username":"scrubdaddy", "email":"example@example.com", "name":"John Doe", "gender":"M", "birthdate":"2000-01-18", "profile_pic":"https://i.pinimg.com/236x/6c/9e/ee/6c9eee49ffdd7a940e3164f424bba803.jpg"}'
    data = json.loads(json_data)
    form = UpdateAccountForm()

    form.email.data = data.get('email')
    form.name.data = data.get('name')
    form.gender.data = data.get('gender')
    form.birthdate.data = data.get('birthdate')
    print(data.get('username'))

    if request.method == 'PUT' and form.validate_on_submit():
        print("do some processing")
    return render_template('user/update_account.html', form=form, userDetails=data)

# DELETE method is somehow (device) restricted in HTML, so lets just use POST.
@user.route('/delete_user', methods=['GET', 'POST'])
def delete_user():
    if request.method == 'POST':
        return redirect(url_for('user.login'))
    else:
        return render_template('errors/403.html')

@user.route('/<string:id>/', methods=['GET', 'POST'])
def view_player_profile(id):
    return render_template('user/view_player_profile.html')