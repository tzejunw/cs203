import os
from . import tournament

from flask import Flask, flash, render_template, request, redirect, url_for
from flask_wtf import FlaskForm 
from wtforms import StringField, PasswordField ,SubmitField 
from wtforms.validators import InputRequired
from werkzeug.security import generate_password_hash 
import requests
from flask import jsonify

@tournament.route('/')
def index():
    return render_template('frontend/index.html')

# SETUP FOR joining a tournament
# @tournament.route('/tournament/join', methods=['POST'])
# def join_tournament():

#     get the jwt token
#     jwt_cookie = request.cookies.get('jwt')
#     # Check if JWT cookie exists (for authentication)
#     if not jwt_cookie:
#       return render_template('error.html', message="You must be logged in to join a tournament"), 401

#     Make a GET request to retrieve the user's username using the JWT token
    # response = requests.get(
    #     current_app.config['BACKEND_URL'] + "/user/get", 
    #     headers={
    #         "Authorization": f"Bearer {jwt_cookie}",
    #         "Content-Type": "application/json"
    #     }
    # )

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
    GOOGLE_MAP_API_KEY = os.getenv('GOOGLE_MAP_API_KEY')
    api_url = f'http://localhost:8080/tournament/get?tournamentName={tournament_name}'
    response = requests.get(api_url)
    
    if response.status_code == 200:
        tournament = response.json()
        return render_template('tournament/tournament.html', tournament=tournament, GOOGLE_MAP_API_KEY=GOOGLE_MAP_API_KEY)
    else:
        return render_template('error.html', message="Tournament not found"), 404


@tournament.route('/pairing')
def view_pairing():
    return render_template('tournament/pairing.html')
    

@tournament.route('/reporting')
def view_reporting():
    return render_template('tournament/reporting.html')

@tournament.route('/standings')
def view_standings():
    return render_template('tournament/standings.html')

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


@tournament.route('/create_player', methods=['GET', 'POST'])
def create_player():

    tournamentName = request.args.get('tournamentName', type = str)
    jwt_cookie = request.cookies.get('jwt')
    headers = {
            'Authorization': f'Bearer {jwt_cookie}',  # Add the JWT token to the header
        }

    #fetch username
    api_url = 'http://localhost:8080/user/get'
    response = requests.get(api_url, headers=headers)
    
    if response.status_code == 200:
        user_data = response.json()
        userName = user_data.get('userName')
        #flash("fetched name", "success")
        print('Username: ' + user_data.get('userName'))
    else:
        print("API call failed with status code:", response.status_code)
        print("Response text:", response.text)

    #create player
    api_url = f'http://localhost:8080/tournament/player/create?tournamentName={tournamentName}&participatingPlayerName={userName}'
    response = requests.post(api_url, headers=headers)

    print('BYEBYE')
    if response.status_code == 200:
        flash("Successfully joined tournament", "success")
    else:
        print("API call failed with status code:", response.status_code)
        print("Response text:", response.text)

    return redirect(request.referrer) #stay on current page
