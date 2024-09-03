from flask_wtf import FlaskForm
from wtforms import StringField, PasswordField, SubmitField, BooleanField, RadioField, SelectField, TextAreaField, DateField, FileField
from wtforms.validators import DataRequired

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
    username = StringField('Username', validators=[DataRequired()])
    password = PasswordField('Password', validators=[DataRequired()])
    login = SubmitField('Login')
    
class RegisterForm(FlaskForm):
    username = StringField('Username', validators=[DataRequired()])
    password = PasswordField('Password', validators=[DataRequired()])
    email = StringField('Email', validators=[DataRequired()])
    gender = RadioField('Gender', choices=[('M', 'Male'), ('F', 'Female')], validators=[DataRequired()])
    birthdate = DateField('Birthdate', format='%Y-%m-%d')
    profile_pic = FileField('Profile Picture')
    register = SubmitField('Register')