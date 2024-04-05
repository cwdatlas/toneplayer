### Tone Player
This little program is used to play a song!
Follow the instructions to play a song!
The song format is note traditional, check the .txt files in the src directory to see examples.

If you would like to see the relation of all the classes and methods, you can look at the TonPlayer.png
in the top level directory of the project.

### Installation
You will need java 17 to run this program

clone the repository into a project directory
```shell script
git clone https://github.com/cwdatlas/toneplayer
```

Move into the newly created directory
```shell script
cd toneplayer
```

Run the program by typing:
- Replace the words DefaultMaryLamb with the name of the song you want to play. 
- If it is in the src folder, you can just type the name without the txt extension
- If it is outside the src folder, you can use the global path without the txt extension
```shell script
ant run -Dfile=DefaultMaryLamb
```

If you have any issues running your song, make sure to read the error message in detail
It will inform you of the available notes and note lengths you can use.

