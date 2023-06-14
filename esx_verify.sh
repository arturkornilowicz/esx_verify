#!/bin/sh
# esx_verify.sh
# 
# Maintainer: Łukasz Szeremeta
# Email: l.szeremeta.dev+mmlkg@gmail.com
# https://github.com/lszeremeta

# Working directory
export WORKDIR=$PWD

# JAR file path
if [ -f /.dockerenv ]; then
    # We're inside Docker
    export JARFILE=$WORKDIR/esx2miz.jar
else
    # Standard config if we're not inside Docker
    export JARFILE=$WORKDIR/target/esx2miz-1.0-jar-with-dependencies.jar
fi

# Input directory
export INPUT=$WORKDIR/input

# Location of esx_mml files
export ESXFILES=$INPUT/esx_mml

# Location of mizar files
export MIZFILES=$WORKDIR/mizar

# Location of mizar binaries
export MIZVER=$MIZFILES/bin

# Location of output files
export OUTPUT=$WORKDIR/output

# Location of temp files
export TEMP=$OUTPUT/temp

# Location of NEWFILES
export NEWFILES=$OUTPUT/NEWFILES

if [ ! -d $TEMP ]; then mkdir $TEMP; fi

if [ ! -d $NEWFILES ]; then mkdir $NEWFILES; fi

# Clearing directories
rm -rf $TEMP/*
rm -rf $NEWFILES/*
rm -rf prel

echo "Copying m.lar (short mml.lar) file to the input directory..."
\cp $WORKDIR/m.lar $INPUT/m.lar
    
if [ "$1" = "download" ]; then
    # Download mml.lar and ESX files
    echo "Downloading mml.lar file..."
    wget --no-check-certificate -O $INPUT/mml.lar https://raw.githubusercontent.com/arturkornilowicz/esx_files/main/mml.lar
    echo "Downloading archive with esx_mml files..."
    wget --no-check-certificate -O $INPUT/main.zip https://github.com/arturkornilowicz/esx_files/archive/refs/heads/main.zip
    echo "Unpacking esx_mml files..."
    if [ ! -d $INPUT/esx_mml ]; then mkdir $INPUT/esx_mml; fi
    unzip -jqo "$INPUT/main.zip" "*esx_mml/*.esx" -d "$INPUT/esx_mml/"
fi

if [ ! -d $INPUT/esx_mml ] || [ ! -n "$(ls -A $INPUT/esx_mml)" ]; then
    echo "No esx_mml directory or no .esx files in input/esx_mml"
fi

if [ ! -e $INPUT/mml.lar ]; then
    echo "No mml.lar file in input directory"
fi

if [ -d $INPUT/esx_mml ] && [ -n "$(ls -A $INPUT/esx_mml)" ] && [ -e $INPUT/mml.lar ]; then
    if [ "$1" = "download" ] && [ -n "$2" ]; then
        MMLLAR="$INPUT/$2"
    elif [ ! "$1" = "download" ] && [ -n "$1" ]; then
        MMLLAR="$INPUT/$1"
    else
        MMLLAR=$INPUT/mml.lar
    fi
else
    echo "Mount a input directory with mml.lar and esx_mml files. You can also mount a output directory to see outputs."
    echo "You can get mml.lar and esx_mml files from https://github.com/arturkornilowicz/esx_files/archive/main.zip"
    echo "Or automatically download and prepare mml.lar and esx_mml files by running the script with the first argument equal to download."
    echo "\n# Required directory structure:"
    echo "input"
    echo "input/mml.lar # order of processing"
    echo "input/esx_mml # directory with .esx files"
    echo "\n# Example:"
    echo "docker run -it --rm -v /path/to/input:/app/input -v /path/to/output:/app/output esx_verify"
    echo "docker run -it --rm -v /path/to/input:/app/input -v /path/to/output:/app/output esx_verify download"
    exit 1
fi

# Order of processing
#MMLLAR=$INPUT/mml.lar

# Generating .miz files based on .esx files

create_miz_file()
{
    filename=`basename $1`
    \cp $1.esx $TEMP
    java -jar $JARFILE $TEMP/$filename.esx
    \cp $MIZFILES/mml/$filename.miz $TEMP
    $MIZVER/msplit $TEMP/$filename
    \mv $TEMP/$filename.new $TEMP/$filename.tpr
    $MIZVER/mglue $TEMP/$filename
    \mv $TEMP/$filename.miz $NEWFILES
    rm -rf $TEMP/$filename.*
}

for i in `cat $MMLLAR`
do
    echo $i
    create_miz_file $ESXFILES/$i
done

# Generating new local database

cd $OUTPUT

\rm -rf prel
\cp -r $WORKDIR/iniprel prel

# Create symlink to mml.ini
ln -s $MIZFILES/mml.ini

# Create symlink to mml.vct
ln -s $MIZFILES/mml.vct

# Create symlink to mizar.dct
ln -s $MIZFILES/mizar.dct

export MIZFILES=$OUTPUT

local_database()
{
    \cp $NEWFILES/$1.miz $TEMP
    $MIZVER/accom $TEMP/$1
    $MIZVER/exporter -l $TEMP/$1
    $MIZVER/transfer -p $TEMP/$1
    rm -rf $TEMP/$1.*
}

echo Creating prels in MIZFILES $MIZFILES

# Articles hidden, tarski_0, and tarski_a generate errors because they define primitives !!!

for i in `cat $MMLLAR`
do
    if [ "$i" != "hidden" ] && [ "$i" != "tarski_0" ] && [ "$i" != "tarski_a" ]
    then       
	local_database $i
    fi    
done

# Verification new files

verify()
{
    \cp $NEWFILES/$1.miz $TEMP
    $MIZVER/accom $TEMP/$1
    $MIZVER/verifier -l $TEMP/$1
    if [ ! $? -eq 0 ]; then echo Error detected; read xxx; fi
    rm -rf $TEMP/$1.*
}

for i in `cat $MMLLAR`
do
    if [ "$i" != "hidden" ] && [ "$i" != "tarski_0" ] && [ "$i" != "tarski_a" ]
    then       
	verify $i
    fi
done
