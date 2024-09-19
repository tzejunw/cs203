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

# to view all tournaments
@tournament.route('/view') #/<int:id>
def view_tournaments():
    api_url = 'http://localhost:8080/tournament/get/all'
    response = requests.get(api_url) 
    tournaments = response.json() 

    return render_template('tournament/tournaments.html', tournaments = tournaments)

# to view an individual tournament
@tournament.route('/tournament')
def view_tournament():
    return render_template('tournament/tournament.html')

# to view rankings
@tournament.route('/rankings')
def view_rankings():
    return render_template('tournament/rankings.html')

@tournament.route('/results')
def view_results():
    return render_template('tournament/results.html')

@tournament.route('/players')
def view_players():
    return render_template('tournament/players.html')

@tournament.route('/matches')
def tournament_matches():
    return render_template('tournament/matches.html')
