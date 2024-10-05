from . import admin


from flask import Flask, render_template, request, redirect, url_for
from flask_wtf import FlaskForm 
from wtforms import StringField, PasswordField ,SubmitField 
from wtforms.validators import InputRequired
from werkzeug.security import generate_password_hash 
import requests
import jwt 

@admin.route('/view_tournaments')
def index():
    
    
    jwt_cookie = request.cookies.get('jwt')
    # Log the jwt_cookie to the console
    print("JWT Cookie:", jwt_cookie)  # This will print the cookie value in the console

    if jwt_cookie:
        try:
            # Decode without verifying the signature
            decoded_jwt = jwt.decode(jwt_cookie, options={"verify_signature": False})
            print("Decoded JWT:", decoded_jwt)
            
            # Assuming the role is stored under a key called 'role'
            is_admin = decoded_jwt.get('admin')
            print("Is admin:", is_admin)

        except jwt.DecodeError:
            print("Error decoding token.")

    return render_template('admin/view_tournaments.html')

