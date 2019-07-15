# cordapp-deterministic-tutorial
Very small example on how deterministic corda compilation and modules work

## How to
Inside sub-module `contracts` the file `ExamapleContract.java` contains a call to `new Random()`. If in 
`contracts/build.gradle` the line `apply from: '../deterministic.gradle'` is commented out, this compiles. 
Otherwise the module is built with the deterministic JDK and the deterministic Corda modules, thus it doesn't compile.
