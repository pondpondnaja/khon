import os
from yolo_video import start
from flask import Flask, flash, request, redirect, url_for, render_template, session, g, Markup
from werkzeug.utils import secure_filename
from utils import detect_image
import base64
import cv2
import numpy as np
import keras.backend as K
import random
import string
import psycopg2


app = Flask(__name__,static_url_path=('/static'))
app.config['SQLAlCHEMY_TRACK_MODIFICATIONS'] = False
app.config['SQLAlCHEMY_DATABASE_URI'] = 'postgres://jlbzqjdoaixcjn:550ecbc824bc536e282ef94ed45d3a61e1a8ac0f1ae6b99b8822ba28451006ec@ec2-54-197-34-207.compute-1.amazonaws.com:5432/d2ce06aa7se8s1'
app.config['SECRET_KEY'] = '550ecbc824bc536e282ef94ed45d3a61e1a8ac0f1ae6b99b8822ba284515140'
app.config['SQLALCHEMY_COMMIT_ON_TEARDOWN'] = True
app.config['SQLALCHEMY_ECHO']=True


'''
class admin_data(db.Model):
    username = db.Column(db.String(20),primary_key = True)
    password = db.Column(db.String(200),unique=False,primary_key = False)
    name = db.Column(db.String(100),unique=False,primary_key = False)
    email = db.Column(db.String(100),unique=False,primary_key = False)
    mobile = db.Column(db.String(20),unique=False,primary_key = False)
    modify_date = db.Column(db.DateTime,unique=False,primary_key = False)
    def __repr__(self):
        return '<admin_data %r>' % self.username
query = admin_data.query.filter_by(username="admin_t").first()
print("\n\n\n\n\n\n\n Heloo",query.username,"\n\n\n\n\n\n\n")
'''

def randomString(stringLength=10):
    """Generate a random string of fixed length"""
    letters = string.ascii_lowercase
    return ''.join(random.choice(letters) for i in range(stringLength))
ALLOWED_EXTENSIONS = set(['png', 'jpg', 'jpeg'])

conn = psycopg2.connect(host='ec2-54-197-34-207.compute-1.amazonaws.com',user='jlbzqjdoaixcjn',password='550ecbc824bc536e282ef94ed45d3a61e1a8ac0f1ae6b99b8822ba28451006ec',database='d2ce06aa7se8s1')
UPLOAD_FOLDER = '/app/static/inputdata/'
app.secret_key= os.urandom(24)
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER

def allowed_file(filename):
    return '.' in filename and \
           filename.rsplit('.', 1)[1] in ALLOWED_EXTENSIONS
    
def format_datetime(value, format='medium'):
    if format == 'full':
        format="EEEE, d. MMMM y 'at' HH:mm"
    elif format == 'medium':
        format="EE dd.MM.y HH:mm"
    return babel.dates.format_datetime(value, format)
app.jinja_env.filters['datetime'] = format_datetime

@app.route('/')
def init():
    with conn:  
        cur = conn.cursor()
        try:
            cur.execute("select info_desc from process where info_num = 1")
            text = cur.fetchall()
            cur.execute("select * from news order by id desc")
            news = cur.fetchall()
        except mysql.Error as e:
            print(e)     
        cur.close()
        i=0
        news_title=[]
        news_desc=[]
        news_subtitle=[]
        news_img_list=[]
        news_index=[]
        while i < len(news):
            news_img_list.append(news[i][4].split(","))
            news_desc.append(Markup(news[i][3]))
            news_subtitle.append(Markup(news[i][2]))
            news_title.append(Markup(news[i][1]))
            news_index.append(Markup(news[i][0]))
            i+=1
        
    return render_template('index.html',news_index=news_index, text = Markup(text[0][0]),news_title=news_title,news_subtitle=news_subtitle,news_desc=news_desc,news_img=news_img_list)
    
@app.route('/gesture')
def phra():
    with conn:
        cur = conn.cursor()
        query = "select * from description"
        cur.execute(query)
        gesture = cur.fetchall()
        cur.close()
    model = []
    i=0
    while i <len(gesture):
        temp = gesture[i][3].split(",")
        print(temp)
        model.append([])
        model[i].append(temp[2])
        model[i].append(temp[3])
        i+=1    
    print(model)
    print (model[0][0])
    print (model[0][1])
    return render_template('gesture.html',gesture = gesture,model=model)

    
@app.route('/show')
def show():
    with conn:
    
        cur = conn.cursor()
        query = "select * from event order by id desc"
        cur.execute(query)
        shows = cur.fetchall()
        cur.close()
        i=0
        shows_title=[]
        shows_desc=[]
        shows_link=[]
        shows_img_list=[]
        shows_index=[]
        shows_location=[]
        
                       
        
        while i < len(shows):
            shows_location.append(Markup(shows[i][5]))
            shows_img_list.append(shows[i][8].split(","))
            shows_desc.append(Markup(shows[i][7]))
            shows_link.append(Markup(shows[i][2]))
            shows_title.append(Markup(shows[i][1]))
            shows_index.append(Markup(shows[i][0]))
            i+=1
    
    
    
    
  
        print("\n\n\n\n\n\n\n\n\n")
        print(shows)
    return render_template('show.html',shows = shows,shows_index=shows_index,shows_title=shows_title,shows_location=shows_location,shows_link=shows_link,shows_desc=shows_desc,shows_img=shows_img_list)
    
@app.route('/login', methods=['GET','POST'])
def login():
    session.pop('admin',None)
    if request.method == 'POST':
        uname_admin=request.form['uname']
        pass_admin =request.form['pass'] 
        print("Receiving: ",uname_admin,pass_admin)
        print("Username: ",str(uname_admin))
        print("Password: ",str(pass_admin))
        with conn: 
            cur = conn.cursor()
            query = "select username,password,name,email from admin_data where username = '"+uname_admin+"'"
            print(query)
            cur.execute(query)   
            login_data = cur.fetchall()
            print (login_data)
            #print (login_data[0][0])
            #print (login_data[0][1])
            if not login_data or login_data[0][1]!=pass_admin:
                return render_template('admin//login_err.html',error="Wrong username or password")
            else:
                session['admin'] = uname_admin
                session['name'] = login_data[0][2]
                session['email'] = login_data[0][3]
                return redirect(url_for('admin'))#render_template('admin//admin_index.html')
            cur.close() 
    return render_template('admin//login.html')
    
@app.route('/admin' ,methods=['GET', 'POST']) 
def admin():
    a=0
    try:
        if session['admin']: 
            if request.method == 'POST':
                try:
                    shows_title = request.form['shows_title']
                    shows_link = request.form['shows_link']
                    shows_date = request.form['shows_date']
                    shows_time = request.form['shows_time']
                    shows_location=request.form['shows_location']
                    shows_desc = request.form['shows_description']
                    shows_image = request.files.getlist("shows_image")
                    shows_folder = '/app/static/images/shows/'
                    name_list=['NA']*len(shows_image)
                    

                    try:
                        i=0
                        
                        while i< len(shows_image):               
                            shows_image_name = shows_image[i]
                            shows_image_name.save(os.path.join(shows_folder,shows_image[i].filename))
                            name_list[i]=(shows_image[i].filename)
                            i+=1
                        name_list= "".join(name_list)
                        name_list=name_list.replace("[","")
                        name_list=name_list.replace("]","")
                        name_list=name_list.replace(".jpg",".jpg,")
                        name_list=name_list.replace(".webp",".webp,")
                    except:
                        name_list = "none.jpg"
                    with conn:
                        cur = conn.cursor()
                        
                        query = 'insert into event(title,link,event_date,event_time,location,description,img_name) values ("'+shows_title+'","'+str(shows_link)+'",(date '+'"'+str(shows_date)+'"),"'+str(shows_time)+'","'+str(shows_location)+'","'+str(shows_desc)+'","'+str(name_list)+'")'
                        print('\n\n\n\n\n\n\n\n\n',query,'Yo\n\n\n\n\n\n\n\n\n')
                        print(query)
                        cur.execute(query)
                        cur.close()
                except:
                    print("not shows insert")
            
                try:
                    shows_edit_id = request.form['shows_edit_id']
                    shows_edit_title = request.form['shows_edit_title']
                    shows_edit_link = request.form['shows_edit_link']
                    shows_edit_date = request.form['shows_edit_date']
                    shows_edit_time = request.form['shows_edit_time']
                    shows_edit_location = request.form['shows_edit_location']
                    shows_edit_description = request.form['shows_edit_description']
                    
                    with conn:
                        query="UPDATE event SET title ='"+str(shows_edit_title)+"', link = '"+str(shows_edit_link)+"',event_date = "+"(date '"+str(shows_edit_date)+"'),event_time = '"+str(shows_edit_time)+"',location = '"+str(shows_edit_location)+"', description = '"+str(shows_edit_description)+"' WHERE event.id = '"+str(shows_edit_id)+"'"
                        print('\n\n\n\n\n\n\n\n\n',query,'\n\n\n\n\n\n\n\n\n')
                        cur = conn.cursor()
                        cur.execute(query)
                        cur.close()
                except:
                    print("not shows edit")
            
                try:
                    shows_delete = request.form['shows_delete']
                    with conn:
                        cur = conn.cursor()
                        query = 'delete from event where id='+str(shows_delete)
                        print(query)
                        cur.execute(query)
                        cur.close()
                except:
                    print("not shows delete")
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
                try:
                    news_title = request.form['news_title']
                    news_subtitle = request.form['news_subtitle']
                    news_desc = request.form['news_description']
                    news_image = request.files.getlist("news_image")
                    news_folder ='/app/static/images/news/'
                    name_list=['NA']*len(news_image)
                    
                    try:
                        i=0
                        
                        while i< len(news_image):               
                            news_image_name = news_image[i]
                            news_image_name.save(os.path.join(news_folder,news_image[i].filename))
                            name_list[i]=(news_image[i].filename)
                            i+=1
                        name_list= "".join(name_list)
                        name_list=name_list.replace("[","")
                        name_list=name_list.replace("]","")
                        name_list=name_list.replace(".jpg",".jpg,")
                        name_list=name_list.replace(".webp",".webp,")
                    except:
                        name_list = "none.jpg"
                    with conn:
                        cur = conn.cursor()
                        query = 'insert into news(title,subtitle,description,img_name) values ("'+news_title+'","'+str(news_subtitle)+'","'+str(news_desc)+'","'+str(name_list)+'")'
                        print(query)
                        cur.execute(query)
                        cur.close()
                except:
                    print("not news insert")
            
                try:
                    news_edit_id = request.form['news_edit_id']
                    news_edit_title = request.form['news_edit_title']
                    news_edit_subtitle = request.form['news_edit_subtitle']
                    news_edit_description = request.form['news_edit_description']
                    print(news_edit_id)
                    print(news_edit_title)
                    print(news_edit_subtitle)
                    print(news_edit_description)
                    with conn:
                        query="UPDATE news SET title ='"+str(news_edit_title)+"', subtitle = '"+str(news_edit_subtitle)+"', description = '"+str(news_edit_description)+"' WHERE news.id = '"+str(news_edit_id)+"'"
                        print('\n\n\n\n\n\n\n\n\n',query,'\n\n\n\n\n\n\n\n\n')
                        cur = conn.cursor()
                        cur.execute(query)
                        cur.close()
                except:
                    print("not news edit")
            
                try:
                    news_delete = request.form['news_delete']
                    with conn:
                        cur = conn.cursor()
                        query = 'delete from news where id='+str(news_delete)
                        print(query)
                        cur.execute(query)
                        cur.close()
                except:
                    print("not news delete")
        
        
            with conn: 
                cur = conn.cursor()
                cur.execute("select * from news order by id")
                news_data = cur.fetchall()
                cur.execute( "select * from event order by id")
                shows_data = cur.fetchall()
                cur.execute("select * from image_in")
                imgin_data = cur.fetchall()
                cur.execute( "select * from image_out")
                imgout_data = cur.fetchall()
                cur.close()
            i=0
            news_title=[]
            news_desc=[]
            news_subtitle=[]
            news_img_list=[]
            news_index=[]
            while i < len(news_data):
                news_img_list.append(news_data[i][4].split(","))
                news_desc.append(Markup(news_data[i][3]))
                news_subtitle.append(Markup(news_data[i][2]))
                news_title.append(Markup(news_data[i][1]))
                news_index.append(Markup(news_data[i][0]))
                i+=1
            i=0
            shows_index=[]
            shows_title=[]
            shows_link=[]
            shows_location=[]
            shows_desc=[]
            shows_img_list=[]
                
            while i < len(shows_data):
                shows_img_list.append(shows_data[i][8].split(","))
                shows_desc.append(Markup(shows_data[i][7]))
                shows_link.append(Markup(shows_data[i][2]))
                shows_title.append(Markup(shows_data[i][1]))
                shows_location.append(Markup(shows_data[i][5]))
                shows_index.append(Markup(shows_data[i][0]))
                i+=1
            print ("\n\n\n\n\n\n\n\n\n\n\n")    
            print (news_img_list) 
            print ("\n\n\n\n\n\n\n\n\n\n\n")              
            print (shows_img_list)
            return render_template('admin//admin_index.html',news_data=news_data,news_img_list=news_img_list,news_desc=news_desc,news_subtitle=news_subtitle,news_title=news_title,news_index=news_index, shows_data = shows_data, imgin_data = imgin_data, imgout_data = imgout_data, name = session['name'],email = session['email'],shows_index=shows_index,shows_title=shows_title,shows_link=shows_link,shows_location=shows_location,shows_desc=shows_desc,shows_img_list=shows_img_list)
    except:
        return redirect(url_for('login'))
    
@app.before_request
def before_request():
    if 'admin' in session:
        g.admin = session['admin']
        print(session['admin'])
    
    
@app.route('/application', methods=['GET', 'POST'])
def hello():
    K.clear_session()
    if request.method == 'POST':
        #algo = request.form['algo']#0
        
        if 'file_input' not in request.files:
            flash('No file part')
            return redirect(request.url)
        file = request.files['file_input']
        if file.filename == '':
            flash('No selected file')
            return redirect(request.url)
        
        
        print(file.filename)
        if file and allowed_file(file.filename): 
            filename = secure_filename(file.filename)
            file.save(os.path.join(app.config['UPLOAD_FOLDER'], filename))
            #file_data = file.stream.read()
            #nparr = np.fromstring(file_data, np.uint8)
            #img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
            #print(img.shape)
            #if algo:
                #yolo = YOLO(class_threshold=0.6, nms_threshold=0.5, algo='tiny_yolo.h5')
                #result = detect_image(cv2.imread(os.path.join(app.config['UPLOAD_FOLDER'], filename)), yolo)
                #result = detect_image(img, yolo)
            path = os.path.join(app.config['UPLOAD_FOLDER'],filename)
            filename, file_extension = os.path.splitext(path)
            #print(path)
            
            
                #print(result[1]) # result will be list of tuple where each tuple is ( class, prob, [box coords])
            #img_str = cv2.imencode('.jpg', result[0])[1].tostring()
            #encoded = base64.b64encode('test').decode("utf-8")
            #mime = "image/jpg;"
            #out_image = f"data:{mime}base64,{encoded}"
                # cv2.imwrite(os.path.join(app.config['RESULT_FOLDER'],filename), result[0])
            #else:
            #    yolo = YOLO(0.8, 0.5, 'tiny_yolo.h5')
                # result = detect_image(cv2.imread(os.path.join(app.config['UPLOAD_FOLDER'], filename)), yolo)
            #    result = detect_image(img, yolo)
             #   print(result[1]) # result will be list of tuple where each tuple is ( class, prob, [box coords])
            #    img_str = cv2.imencode('.jpg', result[0])[1].tostring()
                #encoded = base64.b64encode(img_str).decode("utf-8")
                #mime = "image/jpg;"
                #out_image = f"data:{mime}base64,{encoded}"
                # cv2.imwrite(os.path.join(app.config['RESULT_FOLDER'],filename), result[0])
            
            #hists = os.listdir('static/outputdata')
            #hists = ['plots/' + file for file in hists]
            code= randomString()
            with conn:
                cur = conn.cursor()
                cur.execute("insert into image_in(img_in_id,path) values('"+code+"','"+os.path.join("/static/inputdata/", code,".jpg")+"')")
                path=str(path)
                cur.close()
            path_img,out_class,score_list,gesture,gesture_score = start(path, file_extension, code)
            #out_image = os.path.join(app.config['RESULT_FOLDER'], 'image.jpg')
            print(out_class)
            print("what!",gesture)
            with conn:
                cur = conn.cursor()
                i=0
                while i<len(path_img):
                    cur.execute("insert into image_out(img_out_id,path,class,score) values('"+code+"','"+str(path_img[i].replace("/app",""))+"','"+str(out_class[i])+"','"+str(score_list[i])+"')")
                    i+=1
                cur.execute("select path,class,score from image_out where img_out_id='"+code+"'")
                temp_path=cur.fetchall()
                i=0
                gesture_name=[]
                desc=[]   
                gID=[]
                model=[]
                while i<len(gesture):
                    print("select gid_,name_,desc_,model_ from description where gID='"+str(gesture[i])+"'")
                    cur.execute("select name_,desc_,model_,gid_ from description where gid_='"+str(gesture[i])+"'")
                    temp = cur.fetchall()
                    gesture_name.append(temp[0][0])
                    desc.append(temp[0][1])
                    gID.append(temp[0][3])
                    if out_class[i]=="Ling":  
                        model.append((temp[0][2].split(","))[0])
                    elif out_class[i]=="Yak":
                        model.append((temp[0][2].split(","))[1])
                    elif out_class[i]=="Phra":
                        model.append((temp[0][2].split(","))[2])
                    elif out_class[i]=="Nang":
                        model.append((temp[0][2].split(","))[3])
                    print(model,"\n")
                    print(model[0],"\n")
                    i+=1
                    
                i=0
                found = len(gesture)
                character_thai=[]
                character_eng=[] 
                character_desc = []
                while i<len(gesture):
                    cur.execute("select name_eng,name_thai,description from khon_character where name_eng='"+str(out_class[i])+"'")
                    temp = cur.fetchall()
                    character_eng.append(temp[0][0])
                    character_thai.append(temp[0][1])
                    character_desc.append(temp[0][2])
                    i+=1    
                    
                
                cur.close()
              
            #for i in out_class:
            #print('\n\n\n'+temp_path)
            #print(temp_path)
            out_images=[]
            out_names=[]
            out_scores=[]
            for i in temp_path:
                out_images.append(i[0])
                out_names.append(i[1])
                out_scores.append(i[2])
            print(out_images)
            
            print("Found: ", found)
           
            return render_template('result2.html',gID=gID,model=model,character_desc=character_desc,character_eng=character_eng,character_thai=character_thai, found=found, out_image=out_images,gesture_name=gesture_name, desc = desc, score=out_scores,gesture_score=gesture_score)
        else:
            return "File extension not supported"
    return render_template('application.html')
    #print("endedededed")




if __name__ == '__main__':
    app.secret_key = "key_key"
    app.config['SEND_FILE_MAX_AGE_DEFAULT'] = 0
    app.run(debug=True)