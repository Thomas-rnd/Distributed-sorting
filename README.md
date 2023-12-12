# 434project - Sortnet

CS434 - Course Project

Sortnet is a powerful Scala application designed for distributed and parallel sorting.

**Project Goal:** Implement distributed and parallel sorting of key/value records distributed across multiple machines.

**Input Specification:**
- Each record is 100 bytes in length.
- The first 10 bytes are reserved for a key, crucial for comparing records.
- The remaining 90 bytes store a corresponding value, which is not utilized in the sorting process.

## Installation

Use the interactive build tool for Scala [sbt](https://www.scala-sbt.org/) to compile an run Sortnet.

```bash
cd sortnet/

sbt compile
```

## Usage

### Master

To start the Master, navigate to the sortnet/ directory and use the following command:
```bash
bash bin/master <num_workers>
```
- `<num_workers>`: The number of worker machines to coordinate the sorting process.

### Worker

To start a Worker, navigate to the sortnet/ directory and use the following command:
```bash
bash bin/worker <master_IP:master_PORT> -I <input_directory> <input_directory> ... -O <output_directory> [-ascii]
```
- `<master_IP:master_PORT>`: IP address and port of the Master machine.
- `-I` or `--input`: Specify one or more input directories.
- `-O` or `--output`: Specify the output directory.
- `[-ascii]`: Optional flag to indicate ASCII data type (default is byte).

**Example:**
```bash
cd sortnet/

bash bin/worker 192.168.1.2:5000 -I input_data1 input_data2 -O output_data -ascii
```
**Notes:**

- Ensure the correct format for `<master_IP:master_PORT>` (e.g., `192.168.1.2:5000`).
- At least one input folder must be provided.
- The output directory must be specified.
- The optional `-ascii` flag indicates the use of ASCII data type.

## Logging

The logging configuration for Sortnet is managed through log4j. The log configuration file (`log4j2.xml`) is provided in the project. This configuration defines two appenders:

1. **Console Appender (`console`):**
   - Logs messages to the console.
   - Pattern Layout includes timestamp, log level, thread ID, class name, and the log message.
   - Log level colors are customized for better visibility.

2. **Rolling File Appender (`file`):**
   - Logs messages to a rolling file (`./log/sortnet.log`).
   - Pattern Layout is similar to the console appender.
   - The log file rolls over based on size (10MB), and up to 1000 log files are retained.

The default log level is set to `info`, meaning it logs informational messages and higher levels. Both console and file appenders are used for the root logger.

Adjust the log levels and file paths in the log configuration file based on your specific requirements.

## Test

To ensure the robustness and correctness of Sortnet, we have implemented a comprehensive test suite using the ScalaTest library. The tests cover the core classes as well as the services provided by the master and worker components, excluding network-related methods. This suite aims to verify the functionality of Sortnet's key features, guaranteeing that the sorting algorithm works correctly, and the distributed and parallel processing aspects perform as expected.

To run the tests, navigate to the sortnet/ directory and execute the following command:

```bash
cd sortnet/

# Run test
sbt test
```

This command initiates the test suite, providing detailed output about the success or failure of each individual test. Successful execution of the tests is crucial for ensuring the reliability of Sortnet in various scenarios.

The test suite not only validates the individual components but also checks their interactions to guarantee the seamless coordination of the distributed sorting process. It serves as a fundamental part of our development process, allowing us to catch and address potential issues early on and maintain the overall integrity of Sortnet's functionality.

## Documentation

The documentation for Sortnet is available in the [docs](https://github.com/AlexDevauchelle/434project/tree/main/docs) directory. Please refer to the following documents for detailed information:

- [Data Structure Design](https://github.com/AlexDevauchelle/434project/blob/main/docs/design/data-structure.md)
- [Network Message Structure Design](https://github.com/AlexDevauchelle/434project/blob/main/docs/design/network-msg-structure.md)
- [Master Design](https://github.com/AlexDevauchelle/434project/blob/main/docs/design/master-design.md)
- [Worker Design](https://github.com/AlexDevauchelle/434project/blob/main/docs/design/worker-design.md)

View the [Sequence Diagram](https://github.com/AlexDevauchelle/434project/blob/main/docs/design/Sequence%20Diagram.pdf) for an overview of the process.


## Project Architecture

### 1. `data_scripts`

The data-scripts folder comprises essential shell scripts (*.sh) for managing project data:

#### genData.sh
This script is responsible for generating data on remote machines, supporting both ASCII and byte data types. It accepts several parameters:

```bash
./genData.sh <num_workers> <data_type> <gensort_path> <input_folder_path> <ip_list_txt> [-mf]
```
- <num_workers>: Number of remote machines for data generation.
- <data_type>: Data type, either 'ascii' or 'byte'.
- <gensort_path>: Path to the gensort script on remote machines (e.g., ~/gensort).
- <input_folder_path>: Path to the input data folder on remote machines.
- <ip_list_txt>: List of IP addresses of remote machines.
[-mf]: Optional flag to generate data in multiple folders.
The script checks the existence and emptiness of the input directory on each remote machine before generating data. It calculates the maximum and generated data sizes, creating data files in the specified folder structure.

#### delData.sh
This script facilitates the deletion of data on remote machines, ensuring a clean slate for subsequent data generation. It takes three parameters: the number of workers (<num_workers>), the path to the data folder (<data_folder_path>), and the IP list text file (<ip_list_txt>). Execute the script using:

```bash
./delData.sh <num_workers> <data_folder_path> <ip_list_txt>
```
The script reads IP addresses from the provided text file, checks the range of the specified number of workers, and then proceeds to delete data on each remote machine.

#### valData.sh
This script validates and concatenates summary files generated by Sortnet. Execute the script using:

```bash
./valData.sh <num_workers> <path_to_data> <valsort_path> <ip_list_txt>
```
- <num_workers>: Number of remote machines.
- <path_to_data>: Path to the data directory on remote machines.
- <valsort_path>: Path to the valsort script on remote machines.
- <ip_list_txt>: List of IP addresses of remote machines.
The script checks the existence of the output directory on each remote machine, validates and concatenates summary files, and produces a final summary file (all_final.sum) for further validation. The output folder is set to /tmp/sortnet_OUTPUT. Ensure that this folder exists and is empty before running the script. The validation process checks the integrity of the data and completes with a success message.

### 2. `docs`

The `docs` folder is dedicated to project documentation:

- **Design Documentation:** Contains detailed design documentation for the project.

- **Weekly Progress Reports:** Tracks weekly progress reports on the project.

- **Presentation Slides:** Stores slides used for project-related presentations.

- **Project Milestones:** Provides information about project milestones, including key achievements and goals.

### 3. `sortnet`

The `sortnet` folder houses the main Scala project. Key aspects of the project architecture are defined in the `build.sbt` file within this folder:

- **Project Structure:** Divided into subprojects:
  - `core`: Core functionality.
  - `network`: Networking-related aspects.
  - `master`: Specific to the master component.
  - `worker`: Specific to the worker component.

- **Dependencies:** Dependencies, including log4j2 Scala for logging, are specified in the `build.sbt` file.

- **Scala Version and Organization Details:** Specifies Scala version 2.13.12, project version, and organizational details.

- **Aggregate Settings:** The root project aggregates (`core`, `network`, `master`, `worker`).


## Contributing

Thank you for your interest in this project. Please note that contributions are currently not open. This means that we are not accepting external contributions, pull requests, or issues at this time.

We appreciate your understanding and respect for the project's current development status. If you have any questions or concerns, feel free to reach out [@AlexDevauchelle](https://github.com/AlexDevauchelle) or [@Thomas-rnd](https://github.com/Thomas-rnd).


## License

[MIT](https://choosealicense.com/licenses/mit/)
