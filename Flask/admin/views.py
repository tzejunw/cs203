from flask import Flask, json, render_template, request, redirect, url_for, flash
from flask_wtf import FlaskForm 
import jwt
from wtforms import StringField, SubmitField, DateField
from wtforms.validators import InputRequired
import requests
from flask_wtf.file import FileField


# Assuming you have defined your admin Blueprint somewhere else
from . import admin

class TournamentForm(FlaskForm):
    tournamentName = StringField('Tournament Name', validators=[InputRequired()])
    startDate = StringField('Start Date', validators=[InputRequired()])
    endDate = StringField('End Date', validators=[InputRequired()])
    registrationDeadline = StringField('Registration Deadline', default=None)
    tournamentDesc = StringField('Tournament Description')
    location = StringField('Location')
    imageUrl = FileField('Image', validators=[InputRequired()])
    submit = SubmitField('Create Tournament')

def check_permission(jwt_cookie):
    is_admin = False
    if jwt_cookie:  
        try:
            # Decode without verifying the signature
            decoded_jwt = jwt.decode(jwt_cookie, options={"verify_signature": False})
            print("Decoded JWT:", decoded_jwt)  # Print the decoded JWT for debugging

            is_admin = decoded_jwt.get('admin', False)
        except:
            print("Invalid token")  # Handle decoding error
    return is_admin





@admin.route('/view_tournaments')
def view_tournaments():
    jwt_cookie = request.cookies.get('jwt')
    if not check_permission(jwt_cookie):
        return render_template('errors/403.html', message="Tournament not found"), 403

    #Get page number from query parameter, default is 1
    page = request.args.get('page', 1, type=int)
    page_size = 8

    api_url = 'http://localhost:8080/tournament/get/all'
    response = requests.get(api_url)
    tournaments = response.json()  

    #calculate total number of pages
    total_tournaments = len(tournaments)
    total_pages = (total_tournaments + page_size - 1) // page_size

    start = (page - 1) * page_size
    end = start + page_size
    tournaments_page = tournaments[start:end]

    return render_template('admin/view_tournaments.html', tournaments=tournaments_page, page=page, total_pages=total_pages)

@admin.route('/create_tournament', methods=['GET', 'POST'])
def create_tournament():
    jwt_cookie = request.cookies.get('jwt')
    if not check_permission(jwt_cookie):
        return render_template('errors/403.html', message="Tournament not found"), 403
    
    form = TournamentForm()

    if form.validate_on_submit(): 
        # Prepare tournament data without the image URL
        tournament_data = {
            "tournamentName": form.tournamentName.data,
            "startDate": form.startDate.data,
            "endDate": form.endDate.data,
            "registrationDeadline": form.registrationDeadline.data or None,
            "tournamentDesc": form.tournamentDesc.data,
            "location": form.location.data,
            "adminList": [],  # You might want to populate this with actual admin data
            "participatingPlayers": [],  # Initially empty
            "currentRound": 0

        }

        print(f"Image file: {form.imageUrl.data}")

        api_url = 'http://localhost:8080/tournament/create'
  
        headers = {
            'Authorization': f'Bearer {jwt_cookie}',
            'Content-Type': 'application/json'
        }
        
        # Make the POST request to create the tournament
        response = requests.post(api_url, json=tournament_data, headers=headers)

        if response.status_code in (200, 201):
            # Handle image upload after tournament creation
            image_file = form.imageUrl.data
            if image_file:
                # Create a multipart form-data request
                file = {'file': (image_file.filename, image_file.stream, image_file.mimetype)}
                # Assuming the API provides the tournament ID in the response
                document_id = form.tournamentName.data  # Change if necessary
                upload_url = 'http://localhost:8080/api/image/upload'

                upload_headers = {
                    'Authorization': f'Bearer {jwt_cookie}'
                }

                # Make the POST request to upload the image
                response = requests.post(upload_url, files=file, data={'documentId': document_id}, headers=upload_headers)

                print(f"Image upload response: {response.status_code}, {response.text}")  # Debugging the response



            flash("Tournament created successfully!", "success")
            return redirect(url_for('admin.view_tournaments'))
        else:
            flash("Error creating tournament: " + response.text, "danger")

    return render_template('admin/create_tournament.html', form=form)


@admin.route('/update_tournament', methods=['GET', 'POST'])
def update_tournament():
    jwt_cookie = request.cookies.get('jwt')
    if not check_permission(jwt_cookie):
        return render_template('errors/403.html', message="Tournament not found"), 403
    
    form = TournamentForm()

    if request.method == 'GET':
        tournamentName = request.args.get('tournamentName', type=str)
        api_url = f'http://localhost:8080/tournament/get?tournamentName={tournamentName}'
        response = requests.get(api_url)
        
        if response.status_code == 200:
            tournament_data = response.json()
            form.tournamentName.data = tournament_data.get('tournamentName')
            form.startDate.data = tournament_data.get('startDate')
            form.endDate.data = tournament_data.get('endDate')
            form.registrationDeadline.data = tournament_data.get('registrationDeadline')
            form.tournamentDesc.data = tournament_data.get('tournamentDesc')
            form.location.data = tournament_data.get('location')
            form.imageUrl.data = tournament_data.get('imageUrl')
            print("Response ", tournament_data)

    if form.validate_on_submit():
        # Prepare tournament data without the image URL
        tournament_data = {
            "tournamentName": form.tournamentName.data,
            "startDate": form.startDate.data,
            "endDate": form.endDate.data,
            "registrationDeadline": form.registrationDeadline.data,
            "tournamentDesc": form.tournamentDesc.data,
            "location": form.location.data,
            "adminList": [],  # You might want to populate this with actual admin data
            "participatingPlayers": [],  # Initially empty
            "rounds": None  # You might want to handle this appropriately
        }

        print(f"Image file: {form.imageUrl.data}")

        api_url = 'http://localhost:8080/tournament/update'

        headers = {
            'Authorization': f'Bearer {jwt_cookie}',
            'Content-Type': 'application/json'
        }
        
        # Make the PUT request to update the tournament
        response = requests.put(api_url, json=tournament_data, headers=headers)

        if response.status_code in (200, 201):
            # Handle image upload if an image file is present
            image_file = form.imageUrl.data
            if image_file:
                # Create a multipart form-data request for image upload
                file = {'file': (image_file.filename, image_file.stream, image_file.mimetype)}
                document_id = form.tournamentName.data  # Assuming this is the document ID
                upload_url = 'http://localhost:8080/api/image/upload'

                upload_headers = {
                    'Authorization': f'Bearer {jwt_cookie}'
                }

                # Make the POST request to upload the image
                upload_response = requests.post(upload_url, files=file, data={'documentId': document_id}, headers=upload_headers)

                print(f"Image upload response: {upload_response.status_code}, {upload_response.text}")  # Debugging the response

            flash("Tournament updated successfully!", "success")
            return redirect(url_for('admin.view_tournaments'))
        else:
            flash("Error updating tournament: " + response.text, "danger")

    return render_template('admin/update_tournament.html', form=form)

@admin.route('/delete_tournament/<string:tournament_name>', methods=['POST'])
def delete_tournament(tournament_name):

    jwt_cookie = request.cookies.get('jwt')

    api_url = f'http://localhost:8080/tournament/delete?tournamentName={tournament_name}'

    headers = {
        'Authorization': f'Bearer {jwt_cookie}',
        'Content-Type': 'application/json'
    }
    
    response = requests.delete(api_url, headers=headers)

    print("Response Status Code:", response.status_code)
    print("Response Content:", response.text)

    # Handle response
    if response.status_code == 200 or response.status_code == 204:
        flash(f"{tournament_name} deleted successfully!", "success")
        return redirect(url_for('admin.view_tournaments'))
    else:
        flash("Error deleting tournament: " + response.text, "danger")
        return redirect(url_for('admin.view_tournaments'))

# EVERYTHING BELOW IS FOR ADMIN TOURNAMENT ACTIONS
@admin.route('/manage_tournament/<string:tournament_name>')
def manage_tournament(tournament_name):
    jwt_cookie = request.cookies.get('jwt')  # Retrieve the JWT token
    if not jwt_cookie:
        flash("Unauthorized access.", "danger")
        return redirect(url_for('admin.view_tournaments'))

    # First, fetch the tournament details
    tournament_api_url = f'http://localhost:8080/tournament/get?tournamentName={tournament_name}'
    tournament_response = requests.get(tournament_api_url)

    if tournament_response.status_code != 200:
        flash("Error going to tournament management page: " + tournament_response.text, "danger")
        return redirect(url_for('admin.view_tournaments'))

    tournament = tournament_response.json()

    # Fetch the current round name
    round_name = tournament.get('currentRound')  # Retrieve the current round
    print(tournament_name, round_name)

    # Initialize round_data
    round_data = None

    # Only fetch round data if round_name is valid
    if round_name is not None:
        # Update the round API URL to include the JWT token in the headers
        round_api_url = f'http://localhost:8080/tournament/round/get?tournamentName={tournament_name}&roundName={round_name}'
        round_headers = {
            'Authorization': f'Bearer {jwt_cookie}'  # Include the JWT token in the headers
        }
        round_response = requests.get(round_api_url, headers=round_headers)  # Pass the headers

        if round_response.status_code == 200:
            # Check if the response content is empty
            if round_response.text.strip() == "":  # Check for empty response body
                round_data = None
            else:
                try:
                    round_data = round_response.json()  # Attempt to parse JSON
                    if round_data is None:
                        flash("Round data is null.", "warning")
                    elif not isinstance(round_data, dict):  # Adjust based on expected structure
                        flash("Invalid round data received.", "warning")
                        round_data = None  # Set round_data to None
                except requests.exceptions.JSONDecodeError:
                    flash("Error decoding round data response: Invalid JSON", "danger")
        else:
            flash("Error fetching round data: " + round_response.text, "danger")
            print('####' + round_response.text)

    # Fetch standings for the previous round if round_name is valid
    standings_data = None
    round_name_str = str(int(round_name) - 1) if round_name and round_name.isdigit() else None
    if round_name_str is not None:
        standings_api_url = f'http://localhost:8080/tournament/round/standing/get/all?tournamentName={tournament_name}&roundName={round_name_str}'
        standings_response = requests.get(standings_api_url, headers=round_headers)  # Use the same headers

        if standings_response.status_code == 200:
            standings_data = standings_response.json()
            print("STANDINGS DATA: ")
            print(standings_data)
        else:
            flash("Error fetching standings data: " + standings_response.text, "danger")
    
    round_over = round_data.get('over') if round_data else None

    # Render the page with tournament, round, and standings data
    return render_template(
        'admin/manage_tournament.html', 
        tournament=tournament, 
        round=round_data,  # round_data may be None if no round is available
        standings=standings_data,  # Pass the standings data to the template
        current_round=tournament.get('currentRound'),
        round_over=round_over # Pass in over to the template as well 
    )


# FOR THE ADMIN TO START THE TOURNAMENT
@admin.route('/start_tournament/<string:tournament_name>', methods=['POST'])
def start_tournament(tournament_name):
    jwt_cookie = request.cookies.get('jwt')
    if not check_permission(jwt_cookie):
        return render_template('errors/403.html', message="You do not have permission to start this tournament."), 403

    # Call the Java service to start the tournament
    api_url = f'http://localhost:8080/tournament/start?tournamentName={tournament_name}'
    headers = {
        'Authorization': f'Bearer {jwt_cookie}',
        'Content-Type': 'application/json'
    }

    response = requests.post(api_url, headers=headers)

    if response.status_code in (200, 201):
        flash("Tournament started successfully!", "success")
    else:
        flash("Error starting tournament: " + response.text, "danger")

    return redirect(url_for('admin.manage_tournament', tournament_name=tournament_name))

# FOR THE ADMIN TO END THE TOURNAMENT
@admin.route('/end_tournament/<string:tournament_name>', methods=['POST'])
def end_tournament(tournament_name):
    jwt_cookie = request.cookies.get('jwt')
    if not check_permission(jwt_cookie):
        return render_template('errors/403.html', message="You do not have permission to end this tournament."), 403

    # Call the Java service to end the tournament
    api_url = f'http://localhost:8080/tournament/end?tournamentName={tournament_name}'
    headers = {
        'Authorization': f'Bearer {jwt_cookie}',
        'Content-Type': 'application/json'
    }

    response = requests.post(api_url, headers=headers)

    if response.status_code in (200, 201):
        flash("Tournament ended successfully!", "success")
    else:
        flash("Error ending tournament: " + response.text, "danger")

    return redirect(url_for('admin.manage_tournament', tournament_name=tournament_name))

@admin.route('/toggle_tournament/<string:tournament_name>', methods=['POST'])
def toggle_tournament(tournament_name):
    jwt_cookie = request.cookies.get('jwt')
    if not check_permission(jwt_cookie):
        return render_template('errors/403.html', message="You do not have permission to toggle this tournament."), 403

    # Check if the tournament is in progress
    api_url = f'http://localhost:8080/tournament/get?tournamentName={tournament_name}'
    response = requests.get(api_url)

    if response.status_code == 200:
        tournament = response.json()
        if tournament.get('inProgress'):
            # If tournament is in progress, call the end tournament method
            return end_tournament(tournament_name)
        else:
            # If tournament is not in progress, call the start tournament method
            return start_tournament(tournament_name)
    else:
        flash("Error retrieving tournament details: " + response.text, "danger")
        return redirect(url_for('admin.manage_tournament', tournament_name=tournament_name))

# FOR THE ADMIN TO START/END ROUND


# Admin route to end a specific round in a tournament
@admin.route('/end_round/<string:tournament_name>/<string:round_name>', methods=['POST'])
def end_round(tournament_name, round_name):
    jwt_cookie = request.cookies.get('jwt')
    if not check_permission(jwt_cookie):
        return render_template('errors/403.html', message="You do not have permission to end this round."), 403

    # Call the Java service to end the round
    api_url = f'http://localhost:8080/tournament/round/end?tournamentName={tournament_name}&roundName={round_name}'
    headers = {
        'Authorization': f'Bearer {jwt_cookie}',
        'Content-Type': 'application/json'
    }

    response = requests.get(api_url, headers=headers)

    if response.status_code == 200:
        flash("Round ended successfully!", "success")
    else:
        flash("Error ending round: " + response.text, "danger")

    return redirect(url_for('admin.manage_tournament', tournament_name=tournament_name))


# Admin route to start a new round in a tournament
@admin.route('/start_round/<string:tournament_name>', methods=['POST'])
def start_round(tournament_name):
    jwt_cookie = request.cookies.get('jwt')
    if not check_permission(jwt_cookie):
        return render_template('errors/403.html', message="You do not have permission to start this round."), 403

    # Call the Java service to start a new round
    api_url = f'http://localhost:8080/tournament/round/start?tournamentName={tournament_name}'
    headers = {
        'Authorization': f'Bearer {jwt_cookie}',
        'Content-Type': 'application/json'
    }

    response = requests.get(api_url, headers=headers)

    if response.status_code == 200 and response.json() is True:
        flash("Round started successfully!", "success")
    else:
        flash("Error starting round: " + response.text, "danger")

    return redirect(url_for('admin.manage_tournament', tournament_name=tournament_name))

@admin.route('/toggle_round/<string:tournament_name>', methods=['POST'])
def toggle_round(tournament_name):
    jwt_cookie = request.cookies.get('jwt')
    if not check_permission(jwt_cookie):
        return render_template('errors/403.html', message="You do not have permission to toggle this round."), 403

    # Fetch the tournament details to check the current round name
    tournament_api_url = f'http://localhost:8080/tournament/get?tournamentName={tournament_name}'
    tournament_response = requests.get(tournament_api_url)

    if tournament_response.status_code != 200:
        flash("Error fetching tournament details: " + tournament_response.text, "danger")
        return redirect(url_for('admin.manage_tournament', tournament_name=tournament_name))

    tournament = tournament_response.json()
    
    # Check if tournament data is valid
    if tournament is None or not isinstance(tournament, dict):
        flash("Invalid tournament data received.", "danger")
        return redirect(url_for('admin.manage_tournament', tournament_name=tournament_name))

    round_name = tournament.get('currentRound')  # Retrieve the current round name

    # Check if round_name is None or empty
    if round_name is None or round_name == '':
        flash("No current round to toggle.", "warning")
        return redirect(url_for('admin.manage_tournament', tournament_name=tournament_name))

    # Fetch the round details to check if it's over
    get_round_url = f'http://localhost:8080/tournament/round/get?tournamentName={tournament_name}&roundName={round_name}'
    headers = {
        'Authorization': f'Bearer {jwt_cookie}',
        'Content-Type': 'application/json'
    }

    round_response = requests.get(get_round_url, headers=headers)

    # Check if the round response is valid
    if round_response.status_code == 200:
        try:
            round_data = round_response.json()
            if round_data is None:  # Check if round_data is None
                flash("No round data found.", "warning")
                return redirect(url_for('admin.manage_tournament', tournament_name=tournament_name))
        except requests.exceptions.JSONDecodeError:
            flash("Error: Received an invalid JSON response from the server.", "danger")
            return redirect(url_for('admin.manage_tournament', tournament_name=tournament_name))

        is_over = round_data.get("over", False)

        # Determine the appropriate action based on the current status
        if is_over:
            # Call the end_round method if the round is already in progress
            end_round_url = f'http://localhost:8080/tournament/round/end?tournamentName={tournament_name}&roundName={round_name}'
            action_response = requests.get(end_round_url, headers=headers)
            if action_response.status_code == 200:
                flash("Round ended successfully!", "success")
            else:
                flash("Error ending round: " + action_response.text, "danger")
        else:
            # Call the start_round method if the round is not in progress
            start_round_url = f'http://localhost:8080/tournament/round/start?tournamentName={tournament_name}&roundName={round_name}'  # Include round_name
            action_response = requests.get(start_round_url, headers=headers)
            if action_response.status_code == 200 and action_response.json() is True:
                flash("Round started successfully!", "success")
            else:
                flash("Error starting round: " + action_response.text, "danger")
    else:
        flash("Error fetching round status: " + round_response.text, "danger")

    # Redirect back to the manage tournament page
    return redirect(url_for('admin.manage_tournament', tournament_name=tournament_name))
