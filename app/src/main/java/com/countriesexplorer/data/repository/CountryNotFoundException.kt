package com.countriesexplorer.data.repository

class CountryNotFoundException(val code: String) :
    Exception("Страна с кодом $code не найдена")
