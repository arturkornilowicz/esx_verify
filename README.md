# esx_verify

Checking semantics correctness.
Read more about the MMLKG project at [MMLKG website](https://mmlkg.uwb.edu.pl).

## Shell usage

Build the project with `mvn clean package`.

Run the project with one of the following commands.
You can use `download` param to download mml.lar and esx_mml files and prepare the input directory structure.
You can use smaller `m.lar` file to test the process (it's copied to the input directory automatically).

```shell
./esx_verify.sh download
```

Download mml.lar and esx_mml files and prepare the input directory structure, then run the verification process for all files.


```shell
./esx_verify.sh download m.lar
```

Download mml.lar and esx_mml files and prepare the input directory structure, then run the verification process for small number of files (defined in `m.lar` file; it's copied to the input directory automatically).

```shell
./esx_verify.sh
```

Run the verification process for all files. You need to have `mml.lar` and `esx_mml` directories in the input directory.

```shell
./esx_verify.sh m.lar
```

Run the verification process for small number of files (defined in `m.lar` file; it's copied to the input directory automatically). You need to have `mml.lar` and `esx_mml` directories in the input directory.

## Docker usage

You need to have Docker installed on your machine to be able to build and run the Docker image.

To build the Docker image, run the following command:

```shell
docker build -t esx_verify .
```

Run the Docker image with one of the following commands.
Replace `<input_path>` with directory with mml.lar and esx_mml directory.
You can use `download` param to download mml.lar and esx_mml files and prepare the input directory structure.
You can use smaller `m.lar` file to test the process (it's copied to the input directory automatically).
Replace `<output_path>` with directory if you want to see the output files.

```shell
docker run --rm -v <input_path>:/app/input -v <output_path>:/app/output esx_verify download
```

Download mml.lar and esx_mml files and prepare the input directory structure, then run the verification process for all files.


```shell
docker run --rm -v <input_path>:/app/input -v <output_path>:/app/output esx_verify download m.lar
```

Download mml.lar and esx_mml files and prepare the input directory structure, then run the verification process for small number of files (defined in `m.lar` file; it's copied to the input directory automatically).

```shell
docker run --rm -v <input_path>:/app/input -v <output_path>:/app/output esx_verify
```

Run the verification process for all files. You need to have `mml.lar` and `esx_mml` directories in the input directory.

```shell
docker run --rm -v <input_path>:/app/input -v <output_path>:/app/output esx_verify m.lar
```

Run the verification process for small number of files (defined in `m.lar` file; it's copied to the input directory automatically). You need to have `mml.lar` and `esx_mml` directories in the input directory.
