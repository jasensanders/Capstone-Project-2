# Welcome to My Udacity Android Development  Capstone project!

### This is the app that I submitted as my final project for Udacity's Android Development Nanodegree.
The app is called Folio and it acts as a way to catalog all of your Blu-ray and DVD
movies using only the upc code on the back.  [A more advanced version is available on the play store](https://play.google.com/store/apps/details?id=com.enrandomlabs.jasensanders.v1.folio, "Folio on the Play Store")

### To build this version you will need 2 keys:

1. A Key from The Movie Data Base which you can get here:
	https://www.themoviedb.org/account/signup  -and Sign up.
	Then Click on the "API" link from the left hand sidebar within your account page.
2. A key from Search UPC data base which you can get here:
	http://searchupc.com/developers/ -and Sign up.

Put both of these keys in the strings.xml file under:
	<string name="search_upc_key".....
	    and
	<string name="tmdb_key".....
