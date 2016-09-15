# A tool for identification and structural comparison of descriptors

### Project home page is available [here] [doc].  

DESCS-STANDALONE is a tool allowing user to identify and structurally compare local, contact-based structural motifs, called [*descriptors*] [desc]. The descriptors can be built on unmodified residues from biological molecules such as proteins and RNAs. Both PDB and CIF formats are supported to store 3D structures of the considered molecules. At the beginning of the processing, a comprehensive validation of the input tertiary structures is performed. As a result, all identified inconsistencies are filtered out and stored in a log file. Features of the tool include:

1. Identification of descriptors observed in the structural neighborhood of every residue of the input 3D structure of a molecule.
<<<<<<< HEAD
  - a flexible definition of an expression used for identification of close residues in the structural proximity of a descriptor's center. The tool supports basic operators: logical (i.e. OR, AND, NOT), relational (i.e. <, <=, =, >=, >) and arithmetic ones. A user can introduce the DISTANCE operator between any atoms, except hydrogens, that are found in the 3D structure of the input molecule (e.g. DISTANCE:C1';O5', DISTANCE:CA). Moreover, several virtual atoms can be also applied, i.e. in proteins: geometric centers of a backbone [BBGC] and a side chain [SCGC], CB extended point [CBX], and virtual CB atom provided by biojava [VCB], while in RNAs: geometric centers of a backbone [BBGC], a ribose [RBGC] and a base [BSGC]. 
=======
  - a flexible definition of an expression used for identification of close residues in the structural proximity of a descriptor's center. The tool supports basic operators: logical (i.e., OR, AND, NOT), relational (i.e., <, <=, =, >=, >) and arithmetic ones. A user can introduce the DISTANCE operator between any atoms, except hydrogens, that are found in the 3D structure of the input molecule (e.g., DISTANCE:C1';O5', DISTANCE:CA). Moreover, several virtual atoms can be also applied, i.e., in proteins: geometric centers of a backbone [BBGC] and a side chain [SCGC], CB extended point [CBX], and virtual CB atom provided by biojava [VCB], while in RNAs: geometric centers of a backbone [BBGC], a ribose [RBGC] and a base [BSGC]. 
>>>>>>> refs/heads/release-1.1
  - the size of the descriptor element can be configured by the user.
  - the output descriptors set can be constrained by the user through thresholds associated with the number of segments, elements and residues.
  - a concurrent processing is supported to increase processing efficiency, the number of threads can be configured by the user.

2. A structural comparison of a descriptors pair performed with the use of several computationally efficient algorithms.
<<<<<<< HEAD
  - a backtracking-driven exact algorithms (i.e. BACKTRACKING_DRIVEN_FIRST_ALIGNMENT_ONLY, BACKTRACKING_DRIVEN_LONGEST_ALIGNMENT).
  - hungarian method-driven heuristic algorithms (i.e. HUNGARIAN_METHOD_DRIVEN_FIRST_ALIGNMENT_ONLY_PARTIAL_SOLUTIONS_NOT_CONSIDERED, HUNGARIAN_METHOD_DRIVEN_LONGEST_ALIGNMENT_PARTIAL_SOLUTIONS_NOT_CONSIDERED, HUNGARIAN_METHOD_DRIVEN_LONGEST_ALIGNMENT_PARTIAL_SOLUTIONS_CONSIDERED).
  - thresholds (i.e. a maximal RMSD of the central elements alignment, a maximal RMSD of a pair of aligned duplexes, a minimal fraction of aligned elements, a minimal fraction of aligned residues, a maximal RMSD of the total alignment) driving a multi-criteria function of the structural similarity of descriptors can be flexibly configured by the user.
  - a result of the comparison can be complemented with 3D structures of the aligned descriptors.

3. A format conversion of tertiary structures of considered biological molecules from PDB to CIF and vice versa.
=======
  - a backtracking-driven exact algorithms (i.e., BACKTRACKING_DRIVEN_FIRST_ALIGNMENT_ONLY, BACKTRACKING_DRIVEN_LONGEST_ALIGNMENT).
  - the Hungarian method-driven heuristic algorithms (i.e., HUNGARIAN_METHOD_DRIVEN_FIRST_ALIGNMENT_ONLY_PARTIAL_SOLUTIONS_NOT_CONSIDERED, HUNGARIAN_METHOD_DRIVEN_LONGEST_ALIGNMENT_PARTIAL_SOLUTIONS_NOT_CONSIDERED, HUNGARIAN_METHOD_DRIVEN_LONGEST_ALIGNMENT_PARTIAL_SOLUTIONS_CONSIDERED).
  - thresholds (i.e., a maximal RMSD of the central elements alignment, a maximal RMSD of a pair of aligned duplexes, a minimal fraction of aligned elements, a minimal fraction of aligned residues, a maximal RMSD of the total alignment) driving a multi-criteria function of the structural similarity of descriptors can be flexibly configured by the user.
  - an acceptance criteria, used for identification of potentially better alignment, can be chosen by the user (i.e., ALIGNED_RESIDUES_ONLY, ALIGNED_RESIDUES_AND_AVERAGE_RMSD_OF_ALIGNED_DUPLEXES).   
  - a result of the comparison can be complemented with 3D structures of the aligned descriptors.

3. A format conversion of tertiary structures of considered biological molecules from PDB to CIF and vice versa. 
  - support for generation of EBI-inspired, compatible PDB file bundles (tar.gz) in case of conversion of 3D structures of large biomolecules that are only stored in format CIF.
>>>>>>> refs/heads/release-1.1

An example expression for identification of close residues in the structural proximity of a descriptor's center is presented below:
```sh
OR(DISTANCE:SCGC <= 6.5, AND(DISTANCE:SCGC <= DISTANCE:CA - 0.75, DISTANCE:SCGC <= 8.0))
```
  
### Important dependencies

DESCS-STANDALONE uses a number of external open source projects, namely:

- [BioJava][biojava] - a Java framework for processing biological data,
- [Exp4j][exp4j] - a library dedicated for evaluation of expressions and definition of customized operators,
- [Project Lombok][lombok] - a library allowing compilation and building of a boilerplate-free code,
- [AspectJ][aspectj] - a seamless aspect-oriented extension to Java,
- [jarchivelib][jarchivelib] - an easy-to-use API layer on top of the [org.apache.commons.compress][org.apache.commons.compress].


DESCS-STANDALONE is the open source project available in the [public repository][des-std] on GitHub.

### Requirements

To build the DESCS-STANDALONE package one must have installed: 

- stable release of [Oracle JDK 6] [jdk] or above (however, Oracle JDK 7 is recommended), 
- stable release of [Apache Maven 3.0.3] [mvn] or above, 
- stable release of [Git] [git]. 

A used version of Java can be configured by setting the JAVA_HOME environment variable.

### Installation (common)

```sh
git clone https://github.com/mantczak/descs-standalone.git descs-standalone
cd descs-standalone
```

**_According to an installed version of Oracle JDK, one should adjust the commands presented below with one of the following values "6-7" or "8" introduced instead of constant 'x'_**.

### Build and tests (Windows)

```
build-and-tests-java-x.bat
```

### Build only (Windows)

```
build-only-java-x.bat
```

### Tests only (Windows)

```
tests-only-java-x.bat
```

**_According to configuration of Linux/Mac machine (when maven3 package is installed, and 'No command mvn found') might be a need to add 'mvn3' symlink to 'mvn'._**

### Build and tests (Linux/Mac)

```sh
chmod u+x build-and-tests-java-x.sh
./build-and-tests-java-x.sh
```

### Build only (Linux/Mac)

```sh
chmod u+x build-only-java-x.sh
./build-only-java-x.sh
```

### Tests only (Linux/Mac)

```sh
chmod u+x tests-only-java-x.sh
./tests-only-java-x.sh
```

### Tested configurations

- Linux Ubuntu 14.04 LTS x64, Oracle JDK 1.8.0_73 x64, Apache Maven 3.3.9.
- OS X El Capitan 10.11.3, Oracle JDK 1.7.0_80, Apache Maven 3.3.9.
- Linux Ubuntu 14.04 LTS x64, Open JDK 1.7.0_79 x64, Apache Maven 3.0.5.
- Windows 10 x64, Oracle JDK 1.6.0_45 i586, Apache Maven 3.2.3.
- Linux Mint 11 Katya x64, Oracle JDK 1.6.0_26 x64, Apache Maven 3.0.3.

DESCS-STANDALONE was tested on above configurations, but presumably it will work on other configurations too.

### Acknowledgements

We thank Prof. Krzysztof Fidelis and Andriy Kryshtafovych from the Protein Structure Prediction Center, UC Davis Genome Center, for valuable cooperation, sharing of ideas and discussions.

### Funding

The research was supported by the National Science Centre, Poland [grant No. 2012/05/B/ST6/03026].

License
----
Copyright (c) 2016 PUT Bioinformatics Group, licensed under [MIT license] [mit].

   [desc]: http://onlinelibrary.wiley.com/doi/10.1002/prot.22296/pdf
   [doc]: http://www.cs.put.poznan.pl/mantczak/index.php?slab=descs-standalone
   [biojava]: http://biojava.org
   [exp4j]: http://www.objecthunter.net/exp4j/
   [lombok]: https://projectlombok.org/
   [aspectj]: https://eclipse.org/aspectj/
   [jarchivelib]: http://rauschig.org/jarchivelib/
   [org.apache.commons.compress]: http://commons.apache.org/proper/commons-compress/
   [jdk]: http://java.oracle.com/
   [mvn]: http://maven.apache.org/
   [git]: http://git-scm.com/
   [des-std]: https://github.com/mantczak/descs-standalone.git
   [mit]: http://opensource.org/licenses/mit-license.php
