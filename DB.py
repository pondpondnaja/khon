from flask import Flask, flash, request, redirect, url_for, render_template, session, g, Markup
from flask_sqlalchemy import SQLAlchemy




app = Flask(__name__)
db = SQLAlchemy(app)