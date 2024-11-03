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
import json

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

        user_data = requests.get('http://localhost:8080/user', headers=headers)
        if user_data.status_code == 200:
            user_name = user_data.json().get('userName')
            current_players = tournament.get('participatingPlayers', [])

            # Check if the user is already a participant
            user_joined = user_name in current_players
        else:
            user_joined = False  # Default to False if user data cannot be fetched

        return render_template(
            'tournament/tournament.html',
            tournament=tournament,
            GOOGLE_MAP_API_KEY=GOOGLE_MAP_API_KEY,
            google_calendar_link=generate_google_calendar_link(tournament),
            outlook_calendar_link=generate_outlook_calendar_link(tournament),
            user_joined=user_joined,  # Pass the participation status to the template
            players = current_players
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

# BELOW IS FOR ADDING PLAYERS TO A TOURNAMENT
@tournament.route('/join_tournament', methods=['GET', 'POST'])
def join_tournament():
    tournamentName = request.args.get('tournamentName', type=str)
    jwt_cookie = request.cookies.get('jwt')
    headers = {
        'Authorization': f'Bearer {jwt_cookie}',  # Add the JWT token to the header
    }

    # Fetch username
    api_url = 'http://localhost:8080/user'
    response = requests.get(api_url, headers=headers)

    if response.status_code == 200:
        user_data = response.json()
        userName = user_data.get('userName')
        print('Username:', userName)
    else:
        print("API call failed with status code:", response.status_code)
        print("Response text:", response.text)
        return redirect(request.referrer)

    # Fetch existing tournament names for the user
    existing_tournaments_url = f'http://localhost:8080/tournament/get/forplayer?playerName={userName}'
    existing_tournaments_response = requests.get(existing_tournaments_url, headers=headers)

    if existing_tournaments_response.status_code == 200:
        existing_tournament_names = existing_tournaments_response.json()
        print("Existing tournament names:", existing_tournament_names)  # Debug: Print existing tournament names
    else:
        print("Failed to fetch existing tournaments:", existing_tournaments_response.text)
        return redirect(request.referrer)

    # Fetch new tournament details
    new_tournament_url = f'http://localhost:8080/tournament/get?tournamentName={tournamentName}'
    new_tournament_response = requests.get(new_tournament_url, headers=headers)

    if new_tournament_response.status_code == 200:
        new_tournament = new_tournament_response.json()
        new_start_date = datetime.strptime(new_tournament["startDate"], "%Y-%m-%d")
        new_end_date = datetime.strptime(new_tournament["endDate"], "%Y-%m-%d")

        # Check for overlaps with existing tournaments
        for existing_name in existing_tournament_names:
            # Fetch full details for each existing tournament
            existing_tournament_url = f'http://localhost:8080/tournament/get?tournamentName={existing_name}'
            existing_tournament_response = requests.get(existing_tournament_url, headers=headers)

            if existing_tournament_response.status_code == 200:
                existing_tournament = existing_tournament_response.json()
                existing_start_date = datetime.strptime(existing_tournament["startDate"], "%Y-%m-%d")
                existing_end_date = datetime.strptime(existing_tournament["endDate"], "%Y-%m-%d")

                # Check if the new tournament overlaps with any existing tournament
                if (new_start_date <= existing_end_date) and (new_end_date >= existing_start_date):
                    flash("You cannot join this tournament because it overlaps with an existing tournament.", "danger")
                    return redirect(request.referrer)
            else:
                print(f"Failed to fetch details for tournament {existing_name}: {existing_tournament_response.text}")

    else:
        print("Failed to fetch the new tournament:", new_tournament_response.text)
        return redirect(request.referrer)

    # If no overlap is detected, proceed to join the tournament
    api_url = f'http://localhost:8080/tournament/player/addSelf?tournamentName={tournamentName}'
    response = requests.post(api_url, headers=headers)

    if response.status_code == 200:
        flash(f"You have joined {tournamentName}!", "success")
    else:
        print("API call to join tournament failed with status code:", response.status_code)
        print("Response text:", response.text)
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


