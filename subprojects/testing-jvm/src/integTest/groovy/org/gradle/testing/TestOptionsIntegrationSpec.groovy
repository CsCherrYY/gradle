/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.testing

import org.gradle.integtests.fixtures.AbstractSampleIntegrationTest
import org.gradle.integtests.fixtures.TestResources
import org.junit.Rule

/**
 * These tests demonstrate what is and isn't allowed in terms of modifying the {@link org.gradle.api.tasks.testing.TestFrameworkOptions TestFrameworkOptions}
 * provided to a {@link org.gradle.api.tasks.testing.Test Test} task.
 */
class TestOptionsIntegrationSpec extends AbstractSampleIntegrationTest {
    @Rule
    TestResources resources = new TestResources(temporaryFolder)

    def "can NOT set options and then change framework within a suite using a single task configuration action"() {
        given:
        resources.maybeCopy("TestOptionsIntegrationSpec")
        buildFile << """
        testing {
           suites {
               test {
                   targets {
                       all {
                           testTask.configure {
                               options {
                                   includeCategories 'org.gradle.CategoryA'
                               }
                               useJUnitPlatform()
                           }
                       }
                   }
               }
           }
        }""".stripMargin()

        when:
        fails ":test"

        then:
        result.assertHasErrorOutput("You cannot change the test framework to: JUnit Platform after accessing test options. The current framework is: JUnit 4.")
    }

    def "can NOT set options and then change framework within a suite using 2 different task configuration actions"() {
        given:
        resources.maybeCopy("TestOptionsIntegrationSpec")
        buildFile << """
        testing {
           suites {
               test {
                   targets {
                       all {
                           testTask.configure {
                               options {
                                   includeCategories 'org.gradle.CategoryA'
                               }
                           }
                           testTask.configure {
                               useJUnitPlatform()
                           }
                       }
                   }
               }
           }
        }""".stripMargin()

        when:
        fails ":test"

        then:
        result.assertHasErrorOutput("You cannot change the test framework to: JUnit Platform after accessing test options. The current framework is: JUnit 4.")
    }

    def "can NOT set options in #suiteName prior to calling useJUnitJupiter() on suite"() {
        given:
        resources.maybeCopy("TestOptionsIntegrationSpec")
        buildFile << """
        testing {
           suites {
               $suiteDeclaration {
                   targets {
                       all {
                           testTask.configure {
                               options {
                                   ${type} 'fast'
                               }
                           }
                       }
                   }
                   useJUnitJupiter()
               }
           }
        }""".stripMargin()

        when:
        fails ":$task"

        then:
        result.assertHasErrorOutput("You cannot set the test framework on suite: $suiteName to: JUnit Jupiter after accessing test options on an associated Test task: $task.")

        where:
        suiteName   | suiteDeclaration              | task        | type
        'test'      | 'test'                        | 'test'      | 'includeTags'
        'test'      | 'test'                        | 'test'      | 'excludeTags'
        'integTest' | 'integTest(JvmTestSuite)'     | 'integTest' | 'includeTags'
        'integTest' | 'integTest(JvmTestSuite)'     | 'integTest' | 'excludeTags'
    }

    def "can NOT set options in #suiteName prior to calling useJUnitJupiter() on suite across 2 configurations actions"() {
        given:
        resources.maybeCopy("TestOptionsIntegrationSpec")
        buildFile << """
        testing {
           suites {
               $suiteDeclaration {
                   targets {
                       all {
                           testTask.configure {
                               options {
                                   ${type} 'fast'
                               }
                           }
                       }
                   }
               }
           }
        }
        testing {
           suites {
               $suiteDeclaration {
                   useJUnitJupiter()
               }
           }
        }""".stripMargin()

        when:
        fails ":$task"

        then:
        result.assertHasErrorOutput("You cannot set the test framework on suite: $suiteName to: JUnit Jupiter after accessing test options on an associated Test task: $task.")

        where:
        suiteName   | suiteDeclaration              | task        | type
        'test'      | 'test'                        | 'test'      | 'includeTags'
        'test'      | 'test'                        | 'test'      | 'excludeTags'
        'integTest' | 'integTest(JvmTestSuite)'     | 'integTest' | 'includeTags'
        'integTest' | 'integTest(JvmTestSuite)'     | 'integTest' | 'excludeTags'
    }

    def "can NOT set options on test task directly outside of default test suite, prior to setting test framework inside of suite"() {
        given:
        resources.maybeCopy("TestOptionsIntegrationSpec")
        buildFile << """
        test {
           options {
               includeCategories 'org.gradle.CategoryA'
           }
        }

        testing {
           suites {
               test {
                   useJUnitJupiter()
               }
           }
        }""".stripMargin()

        when:
        fails ":test"

        then:
        result.assertHasErrorOutput("You cannot set the test framework on suite: test to: JUnit Jupiter after accessing test options on an associated Test task: test.")
    }

    def "can NOT set options on test task directly, outside of default test suite, then again inside suite"() {
        given:
        resources.maybeCopy("TestOptionsIntegrationSpec")
        buildFile << """
        test {
           useJUnitPlatform()
           options {
               includeTags 'fast'
           }
        }

        testing {
           suites {
               test {
                   useJUnit() // NOT ALLOWED, task is already configured with a framework, should fail-fast here
               }
           }
        }""".stripMargin()

        when:
        fails ":test"

        then:
        result.assertHasErrorOutput("You cannot set the test framework on suite: test to: JUnit 4 after accessing test options on an associated Test task: test.")
    }

    def "can set non-options test property for test task in #suiteName prior to setting framework within suite"() {
        given:
        resources.maybeCopy("TestOptionsIntegrationSpec")
        buildFile << """
        testing {
           suites {
               $suiteDeclaration {
                   targets {
                       all {
                           testTask.configure {
                               minHeapSize = "128m"
                           }
                       }
                   }
                   useJUnitJupiter()
                   targets {
                       all {
                           testTask.configure {
                               options {
                                   ${type} 'fast'
                               }
                           }
                       }
                   }
               }
           }
        }""".stripMargin()

        expect:
        succeeds ":$task"

        where:
        suiteName   | suiteDeclaration              | task        | type
        'test'      | 'test'                        | 'test'      | 'includeTags'
        'test'      | 'test'                        | 'test'      | 'excludeTags'
        'integTest' | 'integTest(JvmTestSuite)'     | 'integTest' | 'includeTags'
        'integTest' | 'integTest(JvmTestSuite)'     | 'integTest' | 'excludeTags'
    }

    def "can set test framework in test task prior to setting #type option within #suiteName suite"() {
        given:
        resources.maybeCopy("TestOptionsIntegrationSpec")
        buildFile << """
        // Ensure non-default suites exist
        testing {
           suites {
               $suiteDeclaration
            }
        }

        // Configure task directly using name
        $task {
            useJUnitPlatform()
        }

        // Configure task through suite
        testing.suites.$suiteName {
           useJUnitJupiter()
           targets {
               all {
                   testTask.configure {
                       options {
                           ${type} 'fast'
                       }
                   }
               }
           }
        }""".stripMargin()

        expect:
        succeeds ":$task"

        where:
        suiteName   | suiteDeclaration              | task        | type
        'test'      | 'test'                        | 'test'      | 'includeTags'
        'test'      | 'test'                        | 'test'      | 'excludeTags'
        'integTest' | 'integTest(JvmTestSuite)'     | 'integTest' | 'includeTags'
        'integTest' | 'integTest(JvmTestSuite)'     | 'integTest' | 'excludeTags'
    }

    def "can set test framework in #suiteName suite prior to setting #type option within test task"() {
        given:
        resources.maybeCopy("TestOptionsIntegrationSpec")
        buildFile << """
        // Configure task through suite
        testing {
           suites {
               $suiteDeclaration {
                   useJUnitJupiter()
                   dependencies {
                       implementation 'org.junit.jupiter:junit-jupiter-engine:5.4.2'
                   }
               }
           }
        }

        // Configure task directly using name
        $task {
            options {
                ${type} 'fast'
            }
        }

        """.stripMargin()

        expect:
        succeeds ":$task"

        where:
        suiteName   | suiteDeclaration              | task        | type
        'test'      | 'test'                        | 'test'      | 'includeTags'
        'test'      | 'test'                        | 'test'      | 'excludeTags'
        'integTest' | 'integTest(JvmTestSuite)'     | 'integTest' | 'includeTags'
        'integTest' | 'integTest(JvmTestSuite)'     | 'integTest' | 'excludeTags'
    }

    // See JUnitCategoriesIntegrationSpec for the inspiration for this test
    def "re-executes test when #type is changed in #suiteName"() {
        given:
        resources.maybeCopy("TestOptionsIntegrationSpec")
        buildFile << """
        testing {
           suites {
               $suiteDeclaration {
                   useJUnitJupiter()
                   targets {
                       all {
                           testTask.configure {
                               options {
                                   ${type} 'fast'
                               }
                           }
                       }
                   }
               }
           }
        }""".stripMargin()

        when:
        succeeds ":$task"

        then:
        executedAndNotSkipped ":$task"

        when:
        resources.maybeCopy("TestOptionsIntegrationSpec")
        buildFile << """
        testing {
           suites {
               $suiteDeclaration {
                   useJUnitJupiter()
                   targets {
                       all {
                           testTask.configure {
                               options {
                                   ${type} 'slow'
                               }
                           }
                       }
                   }
               }
           }
        }""".stripMargin()

        and:
        succeeds ":$task"

        then:
        executedAndNotSkipped ":$task"

        where:
        suiteName   | suiteDeclaration              | task        | type
        'test'      | 'test'                        | 'test'      | 'includeTags'
        'test'      | 'test'                        | 'test'      | 'excludeTags'
        'integTest' | 'integTest(JvmTestSuite)'     | 'integTest' | 'includeTags'
        'integTest' | 'integTest(JvmTestSuite)'     | 'integTest' | 'excludeTags'
    }

    def "can NOT set new framework for suite in different testing block after configuring options"() {
        buildFile << """
            plugins {
                id 'java'
            }

            ${mavenCentralRepository()}

            testing {
                suites {
                    integrationTest(JvmTestSuite) {
                        useJUnit()
                        targets.all {
                            // explicitly realize the task now to cause this configuration to run now
                            testTask.get().configure {
                                options {
                                    excludeCategories "com.example.Exclude"
                                }
                            }
                        }
                    }
                }
            }

            testing {
                suites {
                    integrationTest {
                        useTestNG()
                    }
                }
            }

            check.dependsOn testing.suites
        """

        when:
        fails("check")

        then:
        result.assertHasErrorOutput("You cannot set the test framework on suite: integrationTest to: Test NG after accessing test options on an associated Test task: integrationTest.")
    }

    def "can NOT change test framework in test task after options have been set within test suites"() {
        buildFile << """
            plugins {
                id 'java'
            }

            ${mavenCentralRepository()}

            testing {
                suites {
                    integrationTest(JvmTestSuite) {
                        useJUnit()
                        targets.all {
                            testTask.configure {
                                options {
                                    excludeCategories "com.example.Exclude"
                                }
                            }
                        }
                    }
                }
            }

            integrationTest {
                useTestNG()
            }

            check.dependsOn testing.suites
        """

        when:
        fails("check")

        then:
        failure.assertHasErrorOutput("You cannot change the test framework to: Test NG after accessing test options. The current framework is: JUnit 4.")
    }
}
