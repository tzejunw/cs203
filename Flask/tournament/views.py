import os
from . import tournament

from flask import Flask, abort, flash, render_template, request, redirect, session, url_for, Response
from flask_wtf import FlaskForm 
from wtforms import StringField, PasswordField ,SubmitField 
from wtforms.validators import InputRequired
from werkzeug.security import generate_password_hash 
import requests
from flask import jsonify
from datetime import datetime

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

def generate_google_calendar_link(tournament):
    start_date_obj = datetime.strptime(tournament["startDate"], "%Y-%m-%d")
    start_date = start_date_obj.strftime("%Y%m%d")

    end_date_obj = datetime.strptime(tournament["endDate"], "%Y-%m-%d")
    end_date = end_date_obj.strftime("%Y%m%d")

    return (
        "https://calendar.google.com/calendar/render?action=TEMPLATE"
        f"&text={tournament["tournamentName"]}"
        f"&dates={start_date}/{end_date}"
        f"&details={tournament["tournamentDesc"]}"
        f"&location={tournament["location"]}"
    )

def generate_outlook_calendar_link(tournament):
    start_date_obj = datetime.strptime(tournament["startDate"], "%Y-%m-%d")
    start_date = start_date_obj.strftime("%Y-%m-%dT00:00:00Z")

    end_date_obj = datetime.strptime(tournament["endDate"], "%Y-%m-%d")
    end_date = end_date_obj.strftime("%Y-%m-%dT00:00:00Z")

    return (
        "https://outlook.live.com/calendar/0/deeplink/compose?"
        f"subject={tournament["tournamentName"]}"
        f"&startdt={start_date}"
        f"&enddt={end_date}"
        f"&body={tournament["tournamentDesc"]}"
        f"&location={tournament["location"]}"
    )

@tournament.route('/download_ics')
def download_ics():
    start_date_str = request.args.get('startDate')
    end_date_str = request.args.get('endDate')
    tournamentName = request.args.get('tournamentName')
    tournamentDesc = request.args.get('tournamentDesc')
    location = request.args.get('location')

    if start_date_str is None or end_date_str is None or location is None or tournamentName is None or tournamentDesc is None:
        abort(404)
    
    start_date_obj = datetime.strptime(start_date_str, "%Y-%m-%d")
    start_date = start_date_obj.strftime("%Y%m%d")

    end_date_obj = datetime.strptime(request.args.get('endDate'), "%Y-%m-%d")
    end_date = end_date_obj.strftime("%Y%m%d")
    
    # Create the .ics file content
    ics_content = f"""BEGIN:VCALENDAR
    VERSION:2.0
    PRODID:-//Magic The Gathering//Arena//EN
    BEGIN:VEVENT
    SUMMARY:{tournamentName}
    DTSTART:{start_date}
    DTEND:{end_date}
    DESCRIPTION:{tournamentDesc}
    LOCATION:{location}
    END:VEVENT
    END:VCALENDAR
    """

    # Serve the file as an attachment
    response = Response(ics_content, mimetype="text/calendar")
    response.headers["Content-Disposition"] = "attachment; filename=event.ics"
    return response
    
@tournament.route('/tournament/<string:tournament_name>')
def view_tournament(tournament_name):
    GOOGLE_MAP_API_KEY = os.getenv('GOOGLE_MAP_API_KEY')
    api_url = f'http://localhost:8080/tournament/get?tournamentName={tournament_name}'
    response = requests.get(api_url)

    if response.status_code == 200:
        tournament = response.json()

        # Check if the user has joined the tournament
        jwt_cookie = request.cookies.get('jwt')
        headers = {
            'Authorization': f'Bearer {jwt_cookie}',  # Add the JWT token to the header
        }

        # Fetch user data
        user_data = requests.get('http://localhost:8080/user', headers=headers)
        if user_data.status_code == 200:
            user_name = user_data.json().get('userName')
            current_players = tournament.get('participatingPlayers', [])
            
            # Check if the user is already a participant
            user_joined = user_name in current_players
        else:
            user_joined = False  # Default to False if user data cannot be fetched

        # Fetch the number of tournaments the user has joined
        tournaments_response = requests.get(f'http://localhost:8080/tournament/get/forplayer?playerName={user_name}', headers=headers)
        if tournaments_response.status_code == 200:
            num_joined = len(tournaments_response.json())
        else:
            flash("Failed to fetch tournaments.", "danger")
            return redirect(request.referrer)

        return render_template(
            'tournament/tournament.html',
            tournament=tournament,
            GOOGLE_MAP_API_KEY=GOOGLE_MAP_API_KEY,
            google_calendar_link=generate_google_calendar_link(tournament),
            outlook_calendar_link=generate_outlook_calendar_link(tournament),
            user_joined=user_joined,
            num_joined=num_joined
        )
    else:
        abort(404)



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

@tournament.route('/my_tournament', methods=['GET'])
def my_tournament():
    tournaments = []  # Empty list to hold tournament data

    # Get JWT token from cookies
    jwt_cookie = request.cookies.get('jwt')
    headers = {
        'Authorization': f'Bearer {jwt_cookie}',  # Add the JWT token to the header
    }

    # Fetch username and tournament names 
    user_data, tournament_names = fetch_user_and_tournaments(headers)

    if not user_data:
        flash("Failed to fetch user", "danger")
        return redirect(request.referrer)

    if not tournament_names:
        flash("You have yet to join any tournaments", "danger")
        return redirect(request.referrer)

    # Fetch tournament details for each tournament name
    tournaments = fetch_tournament_details(tournament_names, headers)
    return render_template('tournament/my_tournament.html', tournaments=tournaments)
   
# FOR USER TO JOIN A TOURNAMENT
@tournament.route('/join_tournament', methods=['GET', 'POST'])
def join_tournament():
    # Get the tournament name from the request arguments
    tournamentName = request.args.get('tournamentName', type=str)

    # Retrieve the JWT token from cookies
    jwt_cookie = request.cookies.get('jwt')
    headers = {
        'Authorization': f'Bearer {jwt_cookie}',  # Include the JWT token in the headers
    }

    # Fetch username using the user API
    api_url = 'http://localhost:8080/user'
    response = requests.get(api_url, headers=headers)

    if response.status_code == 200:
        user_data = response.json()
        userName = user_data.get('userName')

        # Check how many tournaments the user has joined
        tournaments_response = requests.get(f'http://localhost:8080/tournament/get/forplayer?playerName={userName}', headers=headers)
        if tournaments_response.status_code == 200:
            tournament_names = tournaments_response.json()
            # If the user has already joined a tournament, deny the request
            if len(tournament_names) >= 1:
                flash("You can only join one tournament at a time.", "danger")
                return redirect(request.referrer)
        else:
            flash("Failed to fetch tournaments.", "danger")
            return redirect(request.referrer)
    else:
        print("API call failed with status code:", response.status_code)
        print("Response text:", response.text)
        return redirect(request.referrer)

    # Call the new API to add the user to the tournament
    api_url = f'http://localhost:8080/tournament/player/addSelf?tournamentName={tournamentName}'
    response = requests.post(api_url, headers=headers)  # JWT token is included in the headers

    if response.status_code == 200:
        flash(f"You have joined {tournamentName}!", "success")
    else:
        print("API call to join tournament failed with status code:", response.status_code)
        print("Response text:", response.text)
        # Flash a message if joining the tournament fails
        flash("Failed to join the tournament. Please try again.", "danger")

    # Stay on current page
    return redirect(request.referrer)



# Helper functions
def fetch_user_and_tournaments(headers):
    try:
        # Fetch user details
        user_response = requests.get('http://localhost:8080/user', headers=headers)
        if user_response.status_code == 200:
            user_data = user_response.json()
            userName = user_data.get('userName')

            # Fetch tournament names for the player
            tournaments_response = requests.get(f'http://localhost:8080/tournament/get/forplayer?playerName={userName}', headers=headers)
            if tournaments_response.status_code == 200:
                tournament_names = tournaments_response.json()
                return user_data, tournament_names
        return None, None
    except Exception as e:
        print(f"Error fetching user or tournament names: {e}")
        return None, None
    
#Fetches the details for each tournament the user is part of.
def fetch_tournament_details(tournament_names, headers):
    tournaments = []
    for name in tournament_names:
        try:
            tournament_response = requests.get(f'http://localhost:8080/tournament/get?tournamentName={name}', headers=headers)
            if tournament_response.status_code == 200:
                tournaments.append(tournament_response.json()) #append tournament object to list
        except Exception as e:
            print(f"Error fetching tournament details for {name}: {e}")
    return tournaments

