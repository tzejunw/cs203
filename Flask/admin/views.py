from . import admin

from flask import Flask, render_template, request, redirect, url_for
from flask_wtf import FlaskForm 
from wtforms import StringField, PasswordField ,SubmitField 
from wtforms.validators import InputRequired
from werkzeug.security import generate_password_hash 
import requests

@admin.route('/view_tournaments')
def index():
    
    return render_template('admin/view_tournaments.html', voucher_list=data)