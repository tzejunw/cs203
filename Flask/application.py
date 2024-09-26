from user import user
from admin import admin
from tournament import tournament

from flask import Flask, render_template, request, redirect, url_for
from flask_wtf import FlaskForm 
from wtforms import StringField, PasswordField ,SubmitField 
from wtforms.validators import InputRequired
from werkzeug.security import generate_password_hash 

# instance of flask application
application = Flask(__name__)
# application.config['SESSION_COOKIE_NAME'] = None
application.config['SECRET_KEY'] = 'secretkey'

application.register_blueprint(user, url_prefix='/user') 
application.register_blueprint(admin, url_prefix='/admin') 
application.register_blueprint(tournament, url_prefix='/tournament') 

# On each route, check if jwt cookie is still present. So can dynamically display login/logout on navbar
@application.context_processor
def inject_logout_status():
    # Check if a specific cookie exists
    if request.cookies.get('jwt'):  
        return {'jwt_present': True}  
    return {'jwt_present': False}

@application.route('/')
def index():
    return render_template('index.html')

@application.errorhandler(400)
def bad_request(e):
    return render_template('errors/400.html'), 400

@application.errorhandler(401)
def not_authenticated(e):
    return render_template('errors/401.html'), 401

@application.errorhandler(403)
def forbidden(e):
    return render_template('errors/403.html'), 403

@application.errorhandler(404)
def page_not_found(e):
    return render_template('errors/404.html'), 404

if __name__ == '__main__':  
   application.run(debug=True) # remove debug=True for production

if application.debug:
    application.config['BACKEND_URL'] = 'http://localhost:8080'
else:
    application.config['BACKEND_URL'] = 'b'