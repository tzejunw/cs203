from . import tournament

from flask import Flask, render_template, request, redirect, url_for
from flask_wtf import FlaskForm 
from wtforms import StringField, PasswordField ,SubmitField 
from wtforms.validators import InputRequired
from werkzeug.security import generate_password_hash 
import requests

@tournament.route('/')
def index():
    return render_template('frontend/index.html')

@tournament.route('/results')
def results():
    return render_template('tournament/results.html')

@tournament.route('/players')
def players():
    return render_template('tournament/players.html')