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
    # class YOLO defines the default value, so suppress any default here
    parser = argparse.ArgumentParser(argument_default=argparse.SUPPRESS)
    '''
    Command line options
    '''
    parser.add_argument(
        '--model', type=str,
        help='path to model weight file, default ' + YOLO.get_defaults("model_data/model1-tiny_last_weights.h5")
    )

    parser.add_argument(
        '--anchors', type=str,
        help='path to anchor definitions, default ' + YOLO.get_defaults("tiny_yolo_anchors.txt")
    )

    parser.add_argument(
        '--classes', type=str,
        help='path to class definitions, default ' + YOLO.get_defaults("coco_classes.txt")
    )

    parser.add_argument(
        '--gpu_num', type=int,
        help='Number of GPU to use, default ' + str(YOLO.get_defaults("gpu_num"))
    )

    parser.add_argument(
        '--image', default=False, action="store_true",
        help='Image detection mode, will ignore all positional arguments'
    )
    '''
    Command line positional arguments -- for video detection mode
    '''
    parser.add_argument(
        "--input", nargs='?', type=str,required=False,default=path,
        help = "Video input path"
    )

    parser.add_argument(
        "--output", nargs='?', type=str, default="./output",
        help = "[Optional] Video output path"
    )

    FLAGS = parser.parse_args()
    #print(FLAGS.input.shape)
    if file_extension == ".jpg" or file_extension == ".png" or file_extension == ".jpeg" or file_extension == ".JPG" or file_extension == ".JPEG":
        """
        Image detection mode, disregard any remaining command line arguments
        """
        print("Image detection mode")
      
        print('img\nimg\nimg\nimg\nimg\nimg\nimg\nimg\nimg\n')
        print(" Ignoring remaining command line arguments: " + FLAGS.input + "," + FLAGS.output)
        path_img,predicted_class,score_list,gesture,gesture_score = detect_img(YOLO(**vars(FLAGS)),path,code)
        return path_img,predicted_class,score_list,gesture,gesture_score
    elif "input" in FLAGS:
        print(FLAGS.input,'vid\nvid\nvid\nvid\nvid\nvid\nvid\nvid\nvid\n')
        detect_video(YOLO(**vars(FLAGS)), FLAGS.input, FLAGS.output)
    else:
        print("Must specify at least video_input_path.  See usage with --help.")
