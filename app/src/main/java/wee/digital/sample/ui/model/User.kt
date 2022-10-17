package wee.digital.sample.ui.model


class User(
    var firstName: String,
    var lastName: String
) {

    val fullName get() = "%s %s".format(firstName, lastName)
}