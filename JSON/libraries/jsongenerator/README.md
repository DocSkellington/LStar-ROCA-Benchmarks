[Original JSON generator project by Jim Blackler](https://github.com/jimblackler/jsongenerator)

Modified files:
  - `StringUtils.java` to restrict random strings to contain only `[a-zA-Z0-9]*` characters.
  - `Fixer.java` to remove `System.out.println` calls.
  - `Generator.java` to restrict the length arrays and how the depth of the tree is handled (now, the maximum depth is reduced by 1 each time an array or an object starts. This is coherent with our definition of counter value). Moreover, the list of required properties is shuffled, and strings are generated as `\A`, numbers as `\D` and integers as `\I`.