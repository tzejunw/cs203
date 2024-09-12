from flask_wtf import FlaskForm
from wtforms import StringField, PasswordField, SubmitField, BooleanField, RadioField, SelectField, TextAreaField, DateField, FileField
from wtforms.validators import DataRequired

from datetime import datetime

# class ExampleForm(FlaskForm):
#     name = StringField('Name', validators=[DataRequired()])
#     agree = BooleanField('I agree', validators=[DataRequired()])
#     gender = RadioField('Gender', choices=[('M', 'Male'), ('F', 'Female')], validators=[DataRequired()])
#     country = SelectField('Country', choices=[('SG', 'Singapore'), ('MY', 'Malaysia')], validators=[DataRequired()])
#     bio = TextAreaField('Biography')
#     birthdate = DateField('Birthdate', format='%Y-%m-%d')
#     profile_pic = FileField('Profile Picture')
#     submit = SubmitField('Submit')

class LoginForm(FlaskForm):
    email = StringField('Email Address', validators=[DataRequired()])
    password = PasswordField('Password', validators=[DataRequired()])
    login = SubmitField('Login')
    
class RegisterForm(FlaskForm):
    email = StringField('Email', validators=[DataRequired()])
    password = PasswordField('Password', validators=[DataRequired()])
    name = StringField('Full Name', validators=[DataRequired()])
    gender = RadioField('Gender', choices=[('M', 'Male'), ('F', 'Female')], validators=[DataRequired()])
    birthdate = DateField('Birthdate', format='%Y-%m-%d')
    profile_pic = FileField('Profile Picture')
    register = SubmitField('Register')

# Update account form is the same as register form, 
# just that birthdate is different & password and profile_pic is not mandatory.
class UpdateAccountForm(FlaskForm):
    email = StringField('Email', validators=[DataRequired()])
    password = PasswordField('Password (Leave blank to not change it)', validators=[])
    name = StringField('Full Name', validators=[DataRequired()])
    gender = RadioField('Gender', choices=[('M', 'Male'), ('F', 'Female')], validators=[DataRequired()])
    birthdate = DateField('Birthdate', format='%Y-%m-%d')
    profile_pic = FileField('Profile Picture')
    register = SubmitField('Update')