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

# SETUP FOR joining a tournament
# @tournament.route('/tournament/join', methods=['POST'])
# def join_tournament():
#     tournament_name = request.form.get('tournament_name')
#     username = request.form.get('username')  # Assuming user_id is being sent with the form

#     # First, retrieve the tournament to check its current participants
#     api_url = f'http://localhost:8080/tournament/get?tournamentName={tournament_name}'
#     response = requests.get(api_url)

#     if response.status_code == 200:
#         tournament = response.json()
#         if user_id not in tournament.get('participatingPlayers', []):
#             # Add the user to the participatingPlayers list
#             tournament['participatingPlayers'].append(username)
#             # Update the tournament in Firebase
#             update_url = f'http://localhost:8080/tournament/update'
#             update_response = requests.put(update_url, json=tournament)

#             if update_response.status_code == 200:
#                 flash('You have successfully joined the tournament!', 'success')
#             else:
#                 flash('Error updating tournament participants. Please try again.', 'danger')
#         else:
#             flash('You are already participating in this tournament.', 'info')
#     else:
#         flash('Error finding the tournament. Please try again.', 'danger')

#     return redirect(url_for('tournament.view_tournaments'))  # Redirect to the tournament listing page

# to view all tournaments
@tournament.route('/view') #/<int:id>
def view_tournaments():
    api_url = 'http://localhost:8080/tournament/get/all'
    response = requests.get(api_url) 
    tournaments = response.json() 

    return render_template('tournament/tournaments.html', tournaments = tournaments)

@tournament.route('/tournament/<string:tournament_name>')
def view_tournament(tournament_name):
    api_url = f'http://localhost:8080/tournament/get?tournamentName={tournament_name}'
    response = requests.get(api_url)
    location = "SCG CON Portland, OR" # Test data 
    
    if response.status_code == 200:
        tournament = response.json()
        return render_template('tournament/tournament.html', tournament=tournament, location=location)
    else:
        return render_template('error.html', message="Tournament not found"), 404

# to view an individual tournament
# @tournament.route('/tournament')
# def view_tournament():
#     return render_template('tournament/tournament.html')

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
