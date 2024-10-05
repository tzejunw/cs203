from . import admin


from flask import Flask, render_template, request, redirect, url_for
from flask_wtf import FlaskForm 
from wtforms import StringField, PasswordField ,SubmitField 
from wtforms.validators import InputRequired
from werkzeug.security import generate_password_hash 
import requests
import jwt 

@admin.route('/view_tournaments')
def view_tournaments():

    api_url = 'http://localhost:8080/tournament/get/all'
    response = requests.get(api_url)  
    tournaments = response.json()  

    return render_template('admin/view_tournaments.html', tournaments=tournaments)

@admin.route('/create_tournament')
def create_tournament():

 
    return render_template('admin/create_tournament.html')

