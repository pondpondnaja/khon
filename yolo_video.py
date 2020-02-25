import sys
import argparse
from yolo import YOLO
from PIL import Image

def detect_img(yolo,path,code):
    #while True:
        #img = input('Input image filename:')
    print(('Input from path'+ path))
    try:
        print("Noting to do.")
        #image = Image.open(path)
    except:
        print('Open Error! Try again!')
        #continue
    else:
        print(code)
        print(path)
        r_image,predicted_class,score_list,gesture,gesture_score = yolo.detect_image(code,path)
        return (r_image,predicted_class,score_list,gesture,gesture_score)
        #r_image.show()
    yolo.close_session()

FLAGS = None

def start(path,file_extension,code):
    YOLO.get_defaults("model_data/model1-tiny_last_weights.h5")
    YOLO.get_defaults("tiny_yolo_anchors.txt")
    YOLO.get_defaults("coco_classes.txt")
    YOLO.get_defaults("gpu_num")
   
    if file_extension == ".jpg" or file_extension == ".png" or file_extension == ".jpeg" or file_extension == ".JPG" or file_extension == ".JPEG":
        """
        Image detection mode, disregard any remaining command line arguments
        """
        print("Image detection mode")
      
        print('img\nimg\nimg\nimg\nimg\nimg\nimg\nimg\nimg\n')
        
        path_img,predicted_class,score_list,gesture,gesture_score = detect_img(YOLO(),path,code)
        return path_img,predicted_class,score_list,gesture,gesture_score
    elif "input" in FLAGS:
        print(FLAGS.input,'vid\nvid\nvid\nvid\nvid\nvid\nvid\nvid\nvid\n')
        detect_video(YOLO(**vars(FLAGS)), FLAGS.input, FLAGS.output)
    else:
        print("Must specify at least video_input_path.  See usage with --help.")
