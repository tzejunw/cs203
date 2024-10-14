from user import user
from admin import admin
from tournament import tournament

from flask import Flask, render_template, request, redirect, url_for
from flask_wtf import FlaskForm 
from wtforms import StringField, PasswordField ,SubmitField 
from wtforms.validators import InputRequired
from werkzeug.security import generate_password_hash 

import jwt 

# instance of flask application
application = Flask(__name__)
# application.config['SESSION_COOKIE_NAME'] = None
application.config['SECRET_KEY'] = 'secretkey'

application.register_blueprint(user, url_prefix='/user') 
application.register_blueprint(admin, url_prefix='/admin') 
application.register_blueprint(tournament, url_prefix='/tournament') 

# this method is executed for every page that is loaded
@application.context_processor
def inject_login_status():
    jwt_cookie = request.cookies.get('jwt')
    userName = request.cookies.get('userName')
    is_admin = False  # Default to False

    registration = request.cookies.get('registration')
    if registration: # registration not completed
        print("registration not completed")
        return {'registration': True}

    if jwt_cookie:  
        try:
            # Decode without verifying the signature
            decoded_jwt = jwt.decode(jwt_cookie, options={"verify_signature": False})
            print("Decoded JWT:", decoded_jwt)  # Print the decoded JWT for debugging
            
            # Check if the 'admin' key is present and set is_admin accordingly
            is_admin = decoded_jwt.get('admin', False)
            print("Is admin:", is_admin)  # Print the admin status for debugging
        except:
            print("Invalid token")  # Handle decoding error

        return {'jwt_present': True, 'is_admin': is_admin, 'userName': userName}  

    return {'jwt_present': False, 'is_admin': False}

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
    application.config['BACKEND_URL'] = 'b' # TODO: Change with cloud address
