from flask import Flask, redirect, url_for, render_template

app = Flask(__name__)

@app.route("/")
def home():
    return render_template("main.html")

@app.route("/create/")
def create():
    return render_template("create.html")

if __name__ == "__main__":
    app.run(debug=True)