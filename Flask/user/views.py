from . import user
from user.forms import LoginForm, RegisterForm

from flask import Flask, render_template, request, redirect, url_for
from flask_wtf import Form, FlaskForm 
from wtforms import ValidationError, StringField, PasswordField, SubmitField, \
    TextAreaField, BooleanField, RadioField, FileField, DateField, SelectField
from wtforms.validators import DataRequired, Length, EqualTo, Email, URL, AnyOf, Optional
from werkzeug.security import generate_password_hash 
import requests

@user.route('/register')
def register():
    form = RegisterForm();
    return render_template('user/register.html', form=form)

@user.route('/login', methods=['GET', 'POST'])
def login():
    form = LoginForm();
    return render_template('user/login.html', form=form)