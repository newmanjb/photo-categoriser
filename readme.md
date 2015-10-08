I wrote this for myself because it will save me a LOT of hassle when I copy photos from my phone or camera to my main disk and
then have to categorise them all.

It's a java application that can be pointed to a directory containing image files and used to browse the pictures.  Photos
can be moved to directories of the user's choice, and the directory name is automatically prepended with the month and year e.g.
September_2014_TripToVenice, where "TripToVenice" is a name chosen by the user and the rest is generated by the application
based on the photo creation time.  The parent directory for these custom directories is chosen by the user on start-up.

The application uses a command-line interface to browse the photos and categorise them, so no buttons or check-boxes.
It also uses an alias-type system where the name of a directory only has to be put in once and it's then given an integer alias e.g.
typing in "Simon's Leaving Do" will mean that it assigns "1=Simon's Leaving Do", meaning that the user just has to type "1"
and the photo will be copied to this directory.

This will make it extremely fast to use, especially with large numbers of photos, but obviously it's based on my particular way
of categorising them.

Note that the above is in the future tense because although I've written it I haven't started testing it properly yet!
