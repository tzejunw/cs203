from . import frontend

from flask import Flask, render_template, request, redirect, url_for, make_response
from flask_wtf import FlaskForm 
from wtforms import StringField, PasswordField ,SubmitField 
from wtforms.validators import InputRequired
from werkzeug.security import generate_password_hash 
import requests

# home route that returns below text when root url is accessed
@frontend.route('/')
def index():
    return render_template('frontend/index.html')