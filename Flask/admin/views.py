from flask import Flask, json, render_template, request, redirect, url_for, flash
from flask_wtf import FlaskForm 
import jwt
from wtforms import StringField, SubmitField, DateField
from wtforms.validators import InputRequired
import requests

# Assuming you have defined your admin Blueprint somewhere else
from . import admin

class TournamentForm(FlaskForm):
    tournamentName = StringField('Tournament Name', validators=[InputRequired()])
    startDate = StringField('Start Date', validators=[InputRequired()])
    endDate = StringField('End Date', validators=[InputRequired()])
    registrationDeadline = StringField('Registration Deadline', default=None)
    tournamentDesc = StringField('Tournament Description')
    location = StringField('Location')
    imageUrl = StringField('Image URL', validators=[InputRequired()])
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

    print("Form Data:", form.data)  # Add this line

    if form.validate_on_submit():
        tournament_data = {
            "tournamentName": form.tournamentName.data,
            "startDate": form.startDate.data,
            "endDate": form.endDate.data,
            "registrationDeadline": form.registrationDeadline.data if form.registrationDeadline.data else None,
            "tournamentDesc": form.tournamentDesc.data,
            "location": form.location.data,
            "imageUrl": form.imageUrl.data,
            "adminList": [],  # Replace with actual admin emails as needed
            "participatingPlayers": [],  # Initially empty
            "rounds": None
        }

        api_url = 'http://localhost:8080/tournament/create'
  
        headers = {
            'Authorization': f'Bearer {jwt_cookie}',  # Add the JWT token to the header
            'Content-Type': 'application/json'
        }
        response = requests.post(api_url, json=tournament_data, headers=headers)

        # Print the response status code and content for debugging
        print("Response Status Code:", response.status_code)
        print("Response Content:", response.text)

        if response.status_code == 200 or response.status_code == 201:
            flash("Tournament created successfully!", "success")
            return redirect(url_for('admin.view_tournaments'))  # Redirect after successful creation
        else:
            flash("Error creating tournament: " + response.text, "danger")

    return render_template('admin/create_tournament.html', form=form)

# TODO: Secure this route backend
# TODO: GET validations - to ensure name exists
@admin.route('/update_tournament', methods=['GET', 'POST'])
def update_tournament():
    form = TournamentForm()  # Create an instance of the form
    jwt_cookie = request.cookies.get('jwt')
    if not check_permission(jwt_cookie):
        return render_template('errors/403.html', message="Tournament not found"), 403

    if request.method == 'GET':
        tournamentName = request.args.get('tournamentName', type = str)
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
        tournament_data = {
            "tournamentName": form.tournamentName.data,
            "startDate": form.startDate.data,
            "endDate": form.endDate.data,
            "registrationDeadline": form.registrationDeadline.data,
            "tournamentDesc": form.tournamentDesc.data,
            "location": form.location.data,
            "imageUrl": form.imageUrl.data,
            "adminList": [],  # Replace with actual admin emails as needed
            "participatingPlayers": [],  # Initially empty
        }

        api_url = 'http://localhost:8080/tournament/update'
  
        headers = {
            'Authorization': f'Bearer {jwt_cookie}',  # Add the JWT token to the header
            'Content-Type': 'application/json'
        }
        response = requests.put(api_url, json=tournament_data, headers=headers)

        # Print the response status code and content for debugging
        print("Response Status Code:", response.status_code)
        print("Response Content:", response.text)

        if response.status_code == 200 or response.status_code == 201:
            flash("Tournament updated successfully!", "success")
            return redirect(url_for('admin.view_tournaments'))  # Redirect after successful creation
        else:
            flash("Error updating tournament: " + response.text, "danger")
            return render_template('admin/update_tournament.html', form=form)  # Return form in case of error

    return render_template('admin/update_tournament.html',form=form)


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