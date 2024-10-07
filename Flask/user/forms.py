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

class LoginOTPForm(FlaskForm):
    otp = StringField('OTP', validators=[DataRequired()])
    login = SubmitField('Login')
    
class RegisterForm(FlaskForm):
    userName = StringField('Username', validators=[DataRequired()])
    email = StringField('Email', validators=[DataRequired()])
    password = PasswordField('Password', validators=[DataRequired()])
    name = StringField('Full Name', validators=[DataRequired()])
    gender = RadioField('Gender', choices=[('Male', 'Male'), ('Female', 'Female')], validators=[DataRequired()])
    birthday = DateField('Birthday', format='%Y-%m-%d') 
    # profile_pic = FileField('Profile Picture')
    register = SubmitField('Register')

class RegisterStep2Form(FlaskForm):
    userName = StringField('Username', validators=[DataRequired()])
    name = StringField('Full Name', validators=[DataRequired()])
    gender = RadioField('Gender', choices=[('Male', 'Male'), ('Female', 'Female')], validators=[DataRequired()])
    birthday = DateField('Birthday', format='%Y-%m-%d') 
    register = SubmitField('Register')

# Update account form is the same as register form, 
# just that birthdate is different & password and profile_pic is not mandatory.
class UpdateAccountForm(FlaskForm):
    name = StringField('Full Name', validators=[DataRequired()])
    gender = RadioField('Gender', choices=[('Male', 'Male'), ('Female', 'Female')], validators=[DataRequired()])
    birthday = DateField('Birthday', format='%Y-%m-%d')
    # profile_pic = FileField('Profile Picture')
    userName = StringField('', render_kw={'hidden': True})
    register = SubmitField('Update')

# Seperated because need to reconfirm OTP later
class UpdatePasswordForm(FlaskForm):
    password = PasswordField('Password', validators=[DataRequired()])
    register = SubmitField('Update')