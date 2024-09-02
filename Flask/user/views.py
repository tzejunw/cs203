from . import user

from flask import Flask, render_template, request, redirect, url_for
from flask_wtf import FlaskForm 
from wtforms import StringField, PasswordField ,SubmitField 
from wtforms.validators import InputRequired
from werkzeug.security import generate_password_hash 
import requests

@user.route('/register')
def register():
    return render_template('user/register.html')

@user.route('/login')
def login():
    return render_template('user/login.html')