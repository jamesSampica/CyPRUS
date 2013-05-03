/**
 * \file print.cpp
 *
 * Michael Flagg
 * Senior Design May 13-17
 *
 * Outputs text to the console with formatting
 */

#include <stdio.h>

#include <iostream>

#include "print.h"

///Set to true to suppress informational messages
#define PRINT_ONLY_ERRORS false

/**
 * Prints a string in blue. Intended for non-error strings.
 *
 * \param[in] str The string to be printed
 */
void Print::Blue(const std::string &str)
{
    if(!PRINT_ONLY_ERRORS)
    {
        std::cout << "\e[1;34m" << str << "\033[0m";
        fflush(stdout);
    }
}

/**
 * Prints an error string.
 *
 * \param[in] str The string to be printed
 */
void Print::Err(const std::string &str)
{
    std::cout << "\e[1;36mError: " << str << "\033[0m";
    fflush(stdout);
}
