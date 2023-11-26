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
sbt compile
```

## Usage

```bash

# start a Master
bash bin/master NUM_WORKERS

# start Workers
bash bin/worker Master_IP:Master_PORT -I Input_DIR1 Input_DIR2 Input_DIR3 ... -O Output_DIR


```

## Documentation

The documentation for Sortnet is available in the [docs](https://github.com/AlexDevauchelle/434project/tree/main/docs) directory. Please refer to the following documents for detailed information:

- [Data Structure Design](https://github.com/AlexDevauchelle/434project/blob/main/docs/design/data-structure.md)
- [Network Message Structure Design](https://github.com/AlexDevauchelle/434project/blob/main/docs/design/network-msg-structure.md)
- [Master Design](https://github.com/AlexDevauchelle/434project/blob/main/docs/design/master-design.md)
- [Worker Design](https://github.com/AlexDevauchelle/434project/blob/main/docs/design/worker-design.md)

View the [Sequence Diagram](https://github.com/AlexDevauchelle/434project/blob/main/docs/design/Sequence%20Diagram.pdf) for an overview of the process.


## Project Architecture

### 1. `data_scripts`

This folder contains shell scripts (`*.sh`) for managing project data:

- **Data Creation and Verification:** Utilizes [gensort](http://www.ordinal.com/gensort.html) for creating and verifying data. Data is generated in folders like `~/data/input/folder1`, `folder2`, `folder3`, ..., and verification is performed using files in `~/data/output`.

- **Data Deletion:** Includes a delete script that erases all files in `~/data/*`. Users can customize paths using the `-P <your_personal_path>` option.

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
