# JustByHeart (Simple Memorization)

[中文版本](README.md)

## Project Introduction

This is an Android application designed to assist with English vocabulary memorization, abbreviated as "JustByHeart". It helps users efficiently learn and review words based on multiple built-in word banks. The app features a modern Material Design 3 interface, providing a clean and efficient vocabulary memorization experience.

Project URL: [https://github.com/cdz-hy/JustByHeart](https://github.com/cdz-hy/JustByHeart)

### Core Features

- **Daily Word Learning**: Customizable daily learning word count
- **Memorization Testing**: English-Chinese translation multiple-choice questions to reinforce learning outcomes
- **Word Collection**: Collect important words for focused review
- **Learning Records**: View historical learning records by date
- **Progress Tracking**: Real-time display of learning progress and completion status
- **Minimalist Interface**: Material Design 3 design, clean and beautiful

## User Guide

### Core Learning Process

1. **Select Word Bank and Daily Quantity**
   - Switch the desired word bank in the settings interface
   - Set the daily learning word count

2. **Self-testing**
   - In the learning interface, click the word card to flip and hide the Chinese definition for self-testing
   - Flipped words will be placed in the test section

3. **Memorization Marking**
   - For familiar words, you can directly click the [Memorized] button in the lower left corner to skip quickly
   - Words answered correctly in the test module will be automatically marked as memorized

### Main Functional Modules

#### Home Page
- Display the number of words memorized daily, click to enter the current day's word list
- Show overall progress, click to view all memorized/unmemorized words
- Provide quick access to study, search, and review functions

#### Learning Interface
- Memorize and learn in word card units
- Click the card to flip and show/hide the Chinese definition
- Click the [Memorized] button in the lower left corner to quickly mark familiar words

#### Test Module
- Multiple-choice translation test based on flipped words
- Words answered correctly will be automatically marked as mastered

#### Review Interface
- View historical memorization records by day
- Check learning status for specific dates through the date picker

#### Collection Function
- Display collected word content
- Add/cancel word collection at any time

#### Word Bank Module
- View all unmastered and mastered words
- Switch between different status word lists using tabs

#### Search Function
- Support fuzzy search for words
- Display search results in real-time
- Click to view word details

#### Settings Function
- Set daily word learning quantity
- Switch between different word banks
- Support import/export of memorization and collection data
- Support theme color switching

## Technical Architecture

- **Development Language**: Kotlin
- **Architecture Pattern**: MVVM (Model-View-ViewModel)
- **Database**: Room Database
- **UI Framework**: Material Design 3
- **Navigation**: Navigation Component
- **Asynchronous Processing**: Kotlin Coroutines
- **Data Binding**: View Binding

## Acknowledgements

The following open-source project resources were referenced and used during the development of this project:

- Word bank source: [https://github.com/KyleBing/english-vocabulary](https://github.com/KyleBing/english-vocabulary)
- Development assistance tool: [https://github.com/google-gemini/gemini-cli](https://github.com/google-gemini/gemini-cli)

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Contact

If you have any questions or suggestions, please contact us through the following methods:

- Submit a GitHub Issue

---

**Happy Learning!**