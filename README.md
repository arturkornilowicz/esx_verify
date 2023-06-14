# esx_verify

Checking semantics correctness.
Read more about the MMLKG project at [MMLKG website](https://mmlkg.uwb.edu.pl).

## Shell usage

Build the project with `mvn clean package`.

To use it `esx_verify.sh` should be launched.

Some configuration of `esx_verify.sh` is required.

## Docker usage

You need to have Docker installed on your machine to be able to build and run the Docker image.

1. Build the Docker image

To build the Docker image, run the following command:

```shell
docker build -t esx_verify .
```

2. Run the Docker image

To run the Docker image, run the following command:

```shell
docker run --rm -v <input_path>:/app/input -v <output_path>:/app/output esx_verify download
```

or 

```shell
docker run --rm -v <input_path>:/app/input -v <output_path>:/app/output esx_verify download m.lar
```

or

```shell
docker run --rm -v <input_path>:/app/input -v <output_path>:/app/output esx_verify
```

or

```shell
docker run --rm -v <input_path>:/app/input -v <output_path>:/app/output esx_verify m.lar
```

Replace `<input_path>` with directory with mml.lar and esx_mml directory.
You can use `download` param to download mml.lar and esx_mml files and prepare the input directory structure.
You can use smaller `m.lar` file to test the process (it's copied to the input directory automatically).
Replace `<output_path>` with directory if you want to see the output files.
