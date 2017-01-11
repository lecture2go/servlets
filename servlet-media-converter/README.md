# Video Converter
This is a simple video converter with functionality for
- converting of any media to h.264/AAC video
- extracting mp3 audio/AAC from MPEG 4-Video

## How to install
Copy this servlet to your servlet container root directory

## Run
- For MP4
localhost:8080/convertFile?action=convertFileToMp4&id=/path/to/your/video.extention

- For MP3 extraction 
localhost:8080/convertFile?action=extractMp3FromMp4&id=/path/to/your/video.mp4
