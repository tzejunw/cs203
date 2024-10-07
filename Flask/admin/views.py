from flask import Flask, render_template, request, redirect, url_for, flash
from flask_wtf import FlaskForm 
from wtforms import StringField, SubmitField, DateField
from wtforms.validators import InputRequired
import requests

# Assuming you have defined your admin Blueprint somewhere else
from . import admin

class TournamentForm(FlaskForm):
    tournamentName = StringField('Tournament Name', validators=[InputRequired()])
    startDate = StringField('Start Date (MM/DD/YYYY)', validators=[InputRequired()])
    endDate = StringField('End Date (MM/DD/YYYY)', validators=[InputRequired()])
    registrationDeadline = StringField('Registration Deadline (MM/DD/YYYY)', default=None)
    tournamentDesc = StringField('Tournament Description')
    location = StringField('Location')
    imageUrl = StringField('Image URL', validators=[InputRequired()])
    submit = SubmitField('Create Tournament')

@admin.route('/view_tournaments')
def view_tournaments():
    api_url = 'http://localhost:8080/tournament/get/all'
    response = requests.get(api_url)
    tournaments = response.json()  
    return render_template('admin/view_tournaments.html', tournaments=tournaments)

@admin.route('/create_tournament', methods=['GET', 'POST'])
def create_tournament():
    form = TournamentForm()
    jwt_cookie = request.cookies.get('jwt')

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
