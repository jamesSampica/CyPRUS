/**
 * \file print.h
 *
 * Michael Flagg
 * Senior Design May 13-17
 *
 * Handles image capture and camera configuration
 */

#ifndef CONSOLE_OUTPUT_H
#define CONSOLE_OUTPUT_H

#include <string>

class Print
{
public:
    static void Blue(const std::string &str);
    static void Err(const std::string &str);
};

#endif //CONSOLE_OUTPUT_H
