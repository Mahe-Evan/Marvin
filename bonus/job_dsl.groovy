folder("Tools") {
    displayName("Tools")
    description("Folder for miscellaneous tools.")
}

job("Tools/clone-repository") {
    parameters {
        stringParam {
            name("GIT_REPOSITORY_URL")
            description("Git URL of the repository to clone")
        }
    }

    wrappers {
        preBuildCleanup {
            includePattern('.git')
            includePattern('.gitmodules')
            includePattern('target/')
            includePattern('dist/')
            includePattern('build/')
            includePattern('out/')
            includePattern('node_modules/')
            includePattern('venv/')
            includePattern('__pycache__/')
            deleteDirectories()
            cleanupParameter('CLEANUP')
        }
    }

    steps {
        shell('git clone $GIT_REPOSITORY_URL')
    }

    publishers {
        wsCleanup()
    }
}

job("Tools/SEED") {
    displayName("Seed")

    description("""
        This job is used to generate the Jenkins jobs for the Epitech projects.
        It uses the Job DSL plugin to create jobs based on the provided parameters.
        Steps:
        1. Cloning the repository
            - GITHUB_NAME
        2. Checking Coding Style (optional)
            - CHECK_CODING_STYLE
        3. Compiling Project
            - EXECUTABLE_NAME
        4. Checking Unit Tests (optional)
            - CHECK_TESTS
        5. Check Banned Functions (optional)
            - CHECK_BANNED_FUNCTIONS
            - BANNED_FUNCTIONS_MODE
            - BANNED_FUNCTIONS_LIST
        6. Check Memory Leaks (optional)
            - CHECK_MEMORY_LEAKS
            - MEMORY_LEAKS_TEST_COMMANDS
        7. Cleaning up the workspace
    """.stripIndent())

    parameters {
        stringParam {
            name("GITHUB_NAME")
            description("GitHub repository owner/repo_name (e.g.: \"EpitechIT31000/chocolatine\")")
        }

        stringParam {
            name("DISPLAY_NAME")
            description("Display name for the job")
        }

        stringParam {
            name("EXECUTABLE_NAME")
            description("Name of the executable for project compilation")
        }

        booleanParam {
            name("CHECK_CODING_STYLE")
            defaultValue(true)
            description("Check Coding Style of the project")
        }

        booleanParam {
            name("CHECK_TESTS")
            defaultValue(false)
            description("Check Unit Tests of the project")
        }

        booleanParam {
            name("CHECK_BANNED_FUNCTIONS")
            defaultValue(false)
            description("Check Banned Functions of the project")
        }

        booleanParam {
            name("BANNED_FUNCTIONS_MODE")
            description("""Mode for checking banned functions.
- True for Allowed: Check if the project contains any of the allowed functions.
- False for Banned: Check if the project contains any of the banned functions.
            """.stripIndent())
        }

        stringParam {
            name("BANNED_FUNCTIONS_LIST")
            description("""List of functions to check for
- Allowed: List of allowed functions.
- Banned: List of banned functions.
Separated by commas.
Example: "malloc, free, strdup"
            """.stripIndent())
        }

        booleanParam {
            name("CHECK_MEMORY_LEAKS")
            defaultValue(false)
            description("Check Memory Leaks of the project")
        }

        textParam {
            name("MEMORY_LEAKS_TEST_COMMANDS")
            description("""List of commands to check for memory leaks, separated by newlines.
Example:
./my_program arg1 arg2
./my_other_program arg1 arg2
            """.stripIndent())
        }
    }

    steps {
        dsl {
            text('''
                def githubName = binding.variables.GITHUB_NAME
                def displayName = binding.variables.DISPLAY_NAME
                def executableName = binding.variables.EXECUTABLE_NAME
                def checkCodingStyle = binding.variables.CHECK_CODING_STYLE.toBoolean()
                def checkTests = binding.variables.CHECK_TESTS.toBoolean()
                def checkBannedFunctions = binding.variables.CHECK_BANNED_FUNCTIONS.toBoolean()
                def bannedFunctionsMode = binding.variables.BANNED_FUNCTIONS_MODE.toBoolean()
                def bannedFunctionsList = binding.variables.BANNED_FUNCTIONS_LIST
                def checkMemoryLeaks = binding.variables.CHECK_MEMORY_LEAKS.toBoolean()
                def memoryLeaksTestCommands = binding.variables.MEMORY_LEAKS_TEST_COMMANDS

                job(displayName) {
                    parameters {
                        stringParam {
                            name("EXECUTABLE_NAME")
                            defaultValue(executableName)
                            description("Name of the executable for project compilation")
                        }

                        booleanParam {
                            name("CHECK_CODING_STYLE")
                            defaultValue(checkCodingStyle)
                            description("Check Coding Style of the project")
                        }

                        booleanParam {
                            name("CHECK_TESTS")
                            defaultValue(checkTests)
                            description("Check Unit Tests of the project")
                        }

                        booleanParam {
                            name("CHECK_BANNED_FUNCTIONS")
                            defaultValue(checkBannedFunctions)
                            description("Check Banned Functions of the project")
                        }

                        booleanParam {
                            name("BANNED_FUNCTIONS_MODE")
                            defaultValue(bannedFunctionsMode)
                            description("""Mode for checking banned functions.
- True for Allowed: Check if the project contains any of the allowed functions.
- False for Banned: Check if the project contains any of the banned functions.
                            """.stripIndent())
                        }

                        stringParam {
                            name("BANNED_FUNCTIONS_LIST")
                            defaultValue(bannedFunctionsList)
                            description("""List of functions to check for
- Allowed: List of allowed functions.
- Banned: List of banned functions.
Separated by commas.
Example: "malloc, free, strdup"
                            """.stripIndent())
                        }

                        booleanParam {
                            name("CHECK_MEMORY_LEAKS")
                            defaultValue(checkMemoryLeaks)
                            description("Check Memory Leaks of the project")
                        }

                        textParam {
                            name("MEMORY_LEAKS_TEST_COMMANDS")
                            defaultValue(memoryLeaksTestCommands)
                            description("""List of commands to check for memory leaks, separated by newlines.
Example:
./my_program arg1 arg2
./my_other_program arg1 arg2
                            """.stripIndent())
                        }
                    }

                    properties {
                        githubProjectUrl("https://github.com/${githubName}")
                    }

                    triggers {
                        scm('* * * * *')
                    }

                    scm {
                        git {
                            remote {
                                github(githubName)
                                credentials('github-credentials')
                            }
                            branches('*/main', '*/master')
                        }
                    }

                    wrappers {
                        preBuildCleanup()
                    }

                    environmentVariables {
                        env('WORKSPACE', "/mnt/jenkins_builds/${'$'}{JOB_NAME}_${'$'}{BUILD_ID}")
                    }

                    steps {
                        shell("""
                            : "############################################################"
                            : "#                   Setting up Workspace                   #"
                            : "############################################################"
                            mkdir -p /mnt/jenkins_builds
                            mkdir -p ${'$'}WORKSPACE
                            cp -r * ${'$'}WORKSPACE
                        """.stripIndent())

                        shell("""
                            if [ "${'$'}CHECK_CODING_STYLE" = "false" ]; then
                                exit 0
                            fi
                            : "###########################################################"
                            : "#                  Checking Coding Style                  #"
                            : "###########################################################"

                            cd ${'$'}WORKSPACE

                            # Run the coding style checker
                            docker run --rm --security-opt "label:disable" -i \
                            -v "${'$'}WORKSPACE":"/mnt/delivery" \
                            -v "${'$'}WORKSPACE":"/mnt/reports" \
                            ghcr.io/epitech/coding-style-checker:latest "/mnt/delivery" "/mnt/reports"

                            # Check if the report file exists
                            if [ -s "coding-style-reports.log" ]; then
                                cat coding-style-reports.log
                                : "❌ Coding style errors detected! Exiting with status 1."
                                exit 1
                            else
                                : "✅ No coding style issues found."
                            fi

                        """.stripIndent())

                        shell("""
                            : "###########################################################"
                            : "#                    Compiling Project                    #"
                            : "###########################################################"

                            cd ${'$'}WORKSPACE

                            # Run the Docker container
                            docker exec mymarvin-epitest-1 bash -c "cd ${'$'}WORKSPACE && (make clean) > output.txt"
                            cat output.txt
                            docker exec mymarvin-epitest-1 bash -c "cd ${'$'}WORKSPACE && (make) > output.txt"
                            cat output.txt

                            # Check if the executable exists
                            if [ ! -f "${'$'}WORKSPACE/${'$'}EXECUTABLE_NAME" ]; then
                                : "❌ Executable not found! Exiting with status 1."
                                exit 1
                            else
                                : "✅ Executable found: ${'$'}EXECUTABLE_NAME"
                            fi

                            # Clean up the workspace
                            docker exec mymarvin-epitest-1 bash -c "cd ${'$'}WORKSPACE && (make fclean) > output.txt"
                            cat output.txt
                        """.stripIndent())

                        shell("""
                            if [ "${'$'}CHECK_TESTS" = "false" ]; then
                                exit 0
                            fi
                            : "###########################################################"
                            : "#                   Checking Unit Tests                   #"
                            : "###########################################################"

                            cd ${'$'}WORKSPACE

                            # Run the Docker container
                            docker exec mymarvin-epitest-1 bash -c "cd ${'$'}WORKSPACE && (make clean) > output.txt"
                            cat output.txt

                            # Run the unit tests
                            coverage=${'$'}(gcovr --exclude tests/ --json-summary -o coverage.json && grep -o '"line_percent": [0-9.]*' coverage.json | tail -n 1 | grep -o '[0-9.]*')
                            rm -f coverage.json
                            if (( ${'$'}(awk "BEGIN {print (${'$'}coverage < 70)}") )); then
                                rounded_coverage=${'$'}{coverage%.*}
                                echo ": ❌ Coverage is below 70% (Current coverage: ${'$'}{rounded_coverage}%)"
                                exit 1
                            fi
                            echo ": ✅ Coverage is above 70% (Current coverage: ${'$'}{coverage}%)"
                        """.stripIndent())

                        shell("""
                            if [ "${'$'}CHECK_BANNED_FUNCTIONS" = "false" ]; then
                                exit 0
                            fi
                            : "###########################################################"
                            : "#                Checking Banned Functions                #"
                            : "###########################################################"
                            cd ${'$'}WORKSPACE

                            # Create the banned functions check script directly in the Docker container
                            docker exec mymarvin-epitest-1 bash -c "cat > /tmp/banned_functions_check.sh << 'EOF'
                            #!/bin/bash

                            cd ${'$'}WORKSPACE

                            # Checking mode
                            mode=\\\\${'$'}([ \\\\"\\\\${'$'}BANNED_FUNCTIONS_MODE\\\\" = \\\\"true\\\\" ] && echo \\\\"allowed\\\\" || echo \\\\"banned\\\\")
                            if [[ \\\\${'$'}mode != \\\\"allowed\\\\" ]] && [[ \\\\${'$'}mode != \\\\"banned\\\\" ]]; then
                                echo \\\\"Mode '\\\\${'$'}mode' is invalid. Please choose between 'allowed' and 'banned'.\\\\"
                                echo \\\\"::error title=Invalid banned function mode::Mode '\\\\${'$'}mode' is invalid. Please choose between 'allowed' and 'banned'.\\\\"
                                exit 1
                            fi

                            # Checking if nm is installed
                            if ! command -v nm >/dev/null 2>&1; then
                                echo \\\\"::error title=Missing dependency::nm is not installed.\\\\"
                                exit 1
                            fi

                            # Setting up the list of executables
                            executables_list=()
                            IFS=',' read -ra elements <<< \\\\"\\\\${'$'}{EXECUTABLE_NAME}\\\\"
                            for element in \\\\"\\\\${'$'}{elements[@]}\\\\"; do
                                element=\\\\${'$'}(echo \\\\"\\\\${'$'}element\\\\" | xargs | tr '[:upper:]' '[:lower:]')
                                if [[ \\\\"\\\\${'$'}element\\\\" == \\\\"\\\\" ]]; then
                                    continue
                                fi
                                if [[ ! -x \\\\"\\\\${'$'}element\\\\" ]]; then
                                    echo \\\\"Executable '\\\\${'$'}element' does not exist or is not executable.\\\\"
                                    echo \\\\"::error file=\\\\${'$'}{element},title=Executable does not exist or is not executable::Executable '\\\\${'$'}element' does not exist or is not executable.\\\\"
                                    exit 1
                                fi
                                executables_list+=(\\\\"\\\\${'$'}element\\\\")
                            done

                            # Setting up the list of allowed/banned functions
                            functions_list=()
                            IFS=',' read -ra elements <<< \\\\"\\\\${'$'}{BANNED_FUNCTIONS_LIST}\\\\"
                            for element in \\\\"\\\\${'$'}{elements[@]}\\\\"; do
                                element=\\\\"\\\\${'$'}(echo \\\\"\\\\${'$'}element\\\\" | xargs | tr '[:upper:]' '[:lower:]')\\\\"
                                if [[ \\\\"\\\\${'$'}element\\\\" == \\\\"\\\\" ]]; then
                                    continue
                                fi
                                functions_list+=(\\\\"\\\\${'$'}element\\\\")
                            done

                            # Setting up the list of ncurses functions
                            NCURSES_FUNCTIONS=\\\\"addch, addchnstr, addchstr, addnstr, addstr, attroff, attron, attrset, attr_get, attr_off, attr_on, attr_set, baudrate, beep, bkgd, bkgdset, border, box, can_change_color, cbreak, chgat, clear, clearok, clrtobot, clrtoeol, color_content, color_set, COLOR_PAIR, copywin, curs_set, def_prog_mode, def_shell_mode, delay_output, delch, delscreen, delwin, deleteln, derwin, doupdate, dupwin, echo, echochar, erase, endwin, erasechar, filter, flash, flushinp, getbkgd, getch, getnstr, getstr, getwin, halfdelay, has_colors, has_ic, has_il, hline, idcok, idlok, immedok, inch, inchnstr, inchstr, initscr, init_color, init_pair, innstr, insch, insdelln, insertln, insnstr, insstr, instr, intrflush, isendwin, is_linetouched, is_wintouched, keyname, keypad, killchar, leaveok, longname, meta, move, mvaddch, mvaddchnstr, mvaddchstr, mvaddnstr, mvaddstr, mvchgat, mvcur, mvdelch, mvderwin, mvgetch, mvgetnstr, mvgetstr, mvhline, mvinch, mvinchnstr, mvinchstr, mvinnstr, mvinsch, mvinsnstr, mvinsstr, mvinstr, mvprintw, mvscanw, mvvline, mvwaddch, mvwaddchnstr, mvwaddchstr, mvwaddnstr, mvwaddstr, mvwchgat, mvwdelch, mvwgetch, mvwgetnstr, mvwgetstr, mvwhline, mvwin, mvwinch, mvwinchnstr, mvwinchstr, mvwinnstr, mvwinsch, mvwinsnstr, mvwinsstr, mvwinstr, mvwprintw, mvwscanw, mvwvline, napms, newpad, newterm, newwin, nl, nocbreak, nodelay, noecho, nonl, noqiflush, noraw, notimeout, overlay, overwrite, pair_content, PAIR_NUMBER, pechochar, pnoutrefresh, prefresh, printw, putwin, qiflush, raw, redrawwin, refresh, resetty, reset_prog_mode, reset_shell_mode, ripoffline, savetty, scanw, scr_dump, scr_init, scrl, scroll, scrollok, scr_restore, scr_set, setscrreg, set_term, slk_attroff, slk_attr_off, slk_attron, slk_attr_on, slk_attrset, slk_attr, slk_attr_set, slk_clear, slk_color, slk_init, slk_label, slk_noutrefresh, slk_refresh, slk_restore, slk_set, slk_touch, standout, standend, start_color, subpad, subwin, syncok, termattrs, termname, timeout, touchline, touchwin, typeahead, ungetch, untouchwin, use_env, use_tioctl, vidattr, vidputs, vline, vwprintw, vw_printw, vwscanw, vw_scanw, waddch, waddchnstr, waddchstr, waddnstr, waddstr, wattron, wattroff, wattrset, wattr_get, wattr_on, wattr_off, wattr_set, wbkgd, wbkgdset, wborder, wchgat, wclear, wclrtobot, wclrtoeol, wcolor_set, wcursyncup, wdelch, wdeleteln, wechochar, werase, wgetch, wgetnstr, wgetstr, whline, winch, winchnstr, winchstr, winnstr, winsch, winsdelln, winsertln, winsnstr, winsstr, winstr, wmove, wnoutrefresh, wprintw, wredrawln, wrefresh, wscanw, wscrl, wsetscrreg, wstandout, wstandend, wsyncdown, wsyncup, wtimeout, wtouchln, wvline\\\\"
                            ncurses_functions_list=()
                            IFS=',' read -ra elements <<< \\\\"\\\\${'$'}{NCURSES_FUNCTIONS}\\\\"
                            for element in \\\\"\\\\${'$'}{elements[@]}\\\\"; do
                                element=\\\\"\\\\${'$'}(echo \\\\"\\\\${'$'}element\\\\" | xargs)\\\\"
                                if [[ \\\\"\\\\${'$'}element\\\\" == \\\\"\\\\" ]]; then
                                    continue
                                fi
                                ncurses_functions_list+=(\\\\"\\\\${'$'}element\\\\")
                            done

                            # Setting up the list of math functions
                            MATH_FUNCTIONS=\\\\"acos, acosh, asin, asinh, atan, atan2, atanh, cbrt, ceil, copysign, cos, cosh, cospi, cyl_bessel_i0, cyl_bessel_i1, erf, erfc, erfcinv, erfcx, erfinv, exp, exp10, exp2, expm1, fabs, fdim, floor, fma, fmax, fmin, fmod, frexp, hypot, ilogb, j0, j1, jn, ldexp, lgamma, long, long, log, log10, log1p, log2, logb, lrint, lround, modf, nan, nearbyint, nextafter, norm, norm3d, norm4d, normcdf, normcdfinv, pow, powi, rcbrt, remainder, remquo, rhypot, rint, rnorm, rnorm3d, rnorm4d, round, rsqrt, scalbln, scalbn, sin, sincos, sincospi, sinh, sinpi, sqrt, tan, tanh, tgamma, trunc, y0, y1, yn\\\\"
                            math_functions_list=()
                            IFS=',' read -ra elements <<< \\\\"\\\\${'$'}{MATH_FUNCTIONS}\\\\"
                            for element in \\\\"\\\\${'$'}{elements[@]}\\\\"; do
                                element=\\\\"\\\\${'$'}(echo \\\\"\\\\${'$'}element\\\\" | xargs)\\\\"
                                if [[ \\\\"\\\\${'$'}element\\\\" == \\\\"\\\\" ]]; then
                                    continue
                                fi
                                math_functions_list+=(\\\\"\\\\${'$'}element\\\\")
                            done

                            # Function to check if a function is allowed
                            is_allowed() {
                                local function_name=\\\\${'$'}1
                                local source_name=\\\\${'$'}2
                                if [[ \\\\${'$'}mode == \\\\"allowed\\\\" ]]; then
                                    for allowed_function in \\\\"\\\\${'$'}{functions_list[@]}\\\\"; do
                                        if [[ \\\\"\\\\${'$'}function_name\\\\" == \\\\"\\\\${'$'}allowed_function\\\\" ]] || [[ \\\\"\\\\${'$'}source_name\\\\" == \\\\"\\\\${'$'}allowed_function\\\\" ]]; then
                                            return 1
                                        fi
                                        if [[ \\\\${'$'}(echo \\\\"\\\\${'$'}allowed_function\\\\" | tr '[:upper:]' '[:lower:]') == \\\\"ncurses\\\\" ]]; then
                                            for ncurses_function in \\\\"\\\\${'$'}{ncurses_functions_list[@]}\\\\"; do
                                                if [ \\\\"\\\\${'$'}function_name\\\\" == \\\\"\\\\${'$'}ncurses_function\\\\" ]; then
                                                    return 1
                                                fi
                                            done
                                        fi
                                        if [[ \\\\${'$'}(echo \\\\"\\\\${'$'}allowed_function\\\\" | tr '[:upper:]' '[:lower:]') == \\\\"math\\\\" ]]; then
                                            for math_function in \\\\"\\\\${'$'}{math_functions_list[@]}\\\\"; do
                                                if [ \\\\"\\\\${'$'}function_name\\\\" == \\\\"\\\\${'$'}math_function\\\\" ]; then
                                                    return 1
                                                fi
                                            done
                                        fi
                                        if [[ \\\\${'$'}(echo \\\\"\\\\${'$'}allowed_function\\\\" | tr '[:upper:]' '[:lower:]') == \\\\"csfml\\\\" ]] && [[ \\\\"\\\\${'$'}function_name\\\\" == sf* ]]; then
                                            return 1
                                        fi
                                    done
                                    return 0
                                elif [[ \\\\${'$'}mode == \\\\"banned\\\\" ]]; then
                                    for banned_function in \\\\"\\\\${'$'}{functions_list[@]}\\\\"; do
                                        if [[ \\\\"\\\\${'$'}function_name\\\\" == \\\\"\\\\${'$'}banned_function\\\\" ]] || [[ \\\\"\\\\${'$'}source_name\\\\" == \\\\"\\\\${'$'}banned_function\\\\" ]]; then
                                            return 0
                                        fi
                                        if [[ \\\\${'$'}(echo \\\\"\\\\${'$'}banned_function\\\\" | tr '[:upper:]' '[:lower:]') == \\\\"ncurses\\\\" ]]; then
                                            for ncurses_function in \\\\"\\\\${'$'}{ncurses_functions_list[@]}\\\\"; do
                                                if [ \\\\"\\\\${'$'}function_name\\\\" == \\\\"\\\\${'$'}ncurses_function\\\\" ]; then
                                                    return 0
                                                fi
                                            done
                                        fi
                                        if [[ \\\\${'$'}(echo \\\\"\\\\${'$'}banned_function\\\\" | tr '[:upper:]' '[:lower:]') == \\\\"math\\\\" ]]; then
                                            for math_function in \\\\"\\\\${'$'}{math_functions_list[@]}\\\\"; do
                                                if [ \\\\"\\\\${'$'}function_name\\\\" == \\\\"\\\\${'$'}math_function\\\\" ]; then
                                                    return 0
                                                fi
                                            done
                                        fi
                                        if [[ \\\\${'$'}(echo \\\\"\\\\${'$'}banned_function\\\\" | tr '[:upper:]' '[:lower:]') == \\\\"csfml\\\\" ]] && [[ \\\\"\\\\${'$'}function_name\\\\" == sf* ]]; then
                                            return 0
                                        fi
                                    done
                                    return 1
                                fi
                            }

                            # Parsing executables
                            banned_function_detected=0
                            for executable in \\\\"\\\\${'$'}{executables_list[@]}\\\\"; do
                                while IFS= read -r line; do
                                    info=\\\\${'$'}(echo \\\\"\\\\${'$'}line\\\\" | xargs | cut -d ' ' -f 2)
                                    function_name=\\\\${'$'}(echo \\\\"\\\\${'$'}info\\\\" | cut -d '@' -f 1)
                                    source=\\\\${'$'}(echo \\\\"\\\\${'$'}info\\\\" | cut -d '@' -f 2)
                                    source_name=\\\\${'$'}(echo \\\\"\\\\${'$'}source\\\\" | cut -d '_' -f 1 | tr '[:upper:]' '[:lower:]')
                                    source_version=\\\\${'$'}(echo \\\\"\\\\${'$'}source\\\\" | cut -d '_' -f 2)
                                    if [[ \\\\"\\\\${'$'}info\\\\" == \\\\"__gmon_start__\\\\" ]] || [[ \\\\"\\\\${'$'}function_name\\\\" == \\\\"__libc_start_main\\\\" ]]; then
                                        continue
                                    fi
                                    is_allowed \\\\"\\\\${'$'}function_name\\\\" \\\\"\\\\${'$'}source_name\\\\"
                                    if [[ \\\\${'$'}? == 0 ]]; then
                                        banned_function_detected=1
                                        echo \\\\"::error file=\\\\${'$'}{executable},title=Banned function detected::'\\\\${'$'}function_name' is not allowed\\\\"
                                    fi
                                done < <(nm \\\\${'$'}executable -u)
                            done

                            if [[ \\\\${'$'}banned_function_detected == \\\\"1\\\\" ]]; then
                                exit 1
                            else
                                exit 0
                            fi
                            EOF"

                            # Compile the program
                            docker exec mymarvin-epitest-1 bash -c "cd ${'$'}WORKSPACE && (make) > output.txt"
                            cat output.txt

                            # Make the script executable and run it in Docker
                            docker exec mymarvin-epitest-1 bash -c "chmod +x /tmp/banned_functions_check.sh && \
                                export BANNED_FUNCTIONS_MODE='${'$'}{BANNED_FUNCTIONS_MODE}' && \
                                export EXECUTABLE_NAME='${'$'}{EXECUTABLE_NAME}' && \
                                export BANNED_FUNCTIONS_LIST='${'$'}{BANNED_FUNCTIONS_LIST}' && \
                                /tmp/banned_functions_check.sh"

                            # Get the exit code from Docker
                            exit ${'$'}?
                        """.stripIndent())

                        shell("""
                            if [ "${'$'}CHECK_MEMORY_LEAKS" = "false" ]; then
                                exit 0
                            fi
                            : "###########################################################"
                            : "#                  Checking Memory Leaks                  #"
                            : "###########################################################"

                            cd ${'$'}WORKSPACE

                            # Create the memory leaks check script directly in the Docker container
                            docker exec mymarvin-epitest-1 bash -c "cat > /tmp/memory_leaks_check.sh << 'EOF'
                            #!/bin/bash

                            cd ${'$'}WORKSPACE

                            ulimit -n 1024

                            # Setting up the list of executables
                            echo \\\\"\\\\${'$'}MEMORY_LEAKS_TEST_COMMANDS\\\\" | while IFS= read -r command; do
                                [[ -z \\\\"\\\\${'$'}command\\\\" ]] && continue

                                valgrind --leak-check=full --show-leak-kinds=all --track-origins=yes --error-exitcode=1 -s bash -c \\\\"\\\\${'$'}command\\\\" 2>&1 | tee valgrind.log
                                if grep -q \\\\"ERROR SUMMARY: [^0]\\\\" valgrind.log; then
                                    errors=\\\\${'$'}(grep \\\\"ERROR SUMMARY:\\\\" valgrind.log | awk '{print \\\\${'$'}4}')
                                    contexts=\\\\${'$'}(grep \\\\"ERROR SUMMARY:\\\\" valgrind.log | awk '{print \\\\${'$'}7}')
                                    echo \\\\"::error::Memory leaks detected. \\\\${'$'}errors errors from \\\\${'$'}contexts contexts\\\\"
                                    rm valgrind.log
                                    exit 1
                                fi
                                rm valgrind.log
                            done
                            EOF"

                            # Compile the program
                            docker exec mymarvin-epitest-1 bash -c "cd ${'$'}WORKSPACE && (make) > output.txt"
                            cat output.txt

                            # Make the script executable and run it in Docker
                            docker exec mymarvin-epitest-1 bash -c "chmod +x /tmp/memory_leaks_check.sh && \
                                export MEMORY_LEAKS_TEST_COMMANDS='${'$'}{MEMORY_LEAKS_TEST_COMMANDS}' && \
                                /tmp/memory_leaks_check.sh"

                            # Get the exit code from Docker
                            exit ${'$'}?



                        """.stripIndent())

                        shell("""
                            : "###########################################################"
                            : "#                  Cleaning up Workspace                  #"
                            : "###########################################################"
                            rm -rf ${'$'}WORKSPACE
                        """.stripIndent())
                    }
                }
            ''')
        }
    }
}
