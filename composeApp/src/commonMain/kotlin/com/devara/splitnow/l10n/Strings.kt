package com.devara.splitnow.l10n

/**
 * Source-of-truth English string catalogue. The Translator service uses this
 * list as the input to a one-shot Gemini call when the user picks a non-English
 * locale, batch-translates everything, and caches the result keyed by locale.
 *
 * Composable code reads strings via `tr(Strings.someKey)` which falls back to
 * the English source if the cache hasn't been populated yet.
 */
object Strings {
    // Onboarding
    const val GET_STARTED = "Get started"

    // Home
    const val NEW_SPLIT = "New split"
    const val SPLITS_THIS_MONTH = "splits this month"
    const val HISTORY = "History"

    // Scan
    const val AUTO_DETECT = "Auto-detect receipt"
    const val HOLD_STEADY = "Hold steady. We'll snap it for you."
    const val PHOTOS = "Photos"

    // Describe
    const val WHO_GOT_WHAT = "Who got what?"
    const val DESCRIBE_HINT = "Type naturally. AI figures out who ordered what — no need to add people first."
    const val DESCRIBE_PLACEHOLDER = "e.g. Budi got the nasi goreng pedas and an es teh. Tina ordered nasi goreng seafood. Joko only had air putih. We shared the kerupuk."
    const val SPLIT_IT = "Split it"

    // Loading
    const val SPLITTING_BILL = "Splitting\nyour bill"
    const val USUALLY_TAKES = "Usually takes 2–3 seconds."

    // Review
    const val REVIEW = "Review"
    const val PER_PERSON = "Per person"
    const val SHARED = "Shared"
    const val TAX_CHARGES = "Tax & charges"
    const val SPLIT_TAX_SERVICE = "Split tax & service"
    const val PROPORTIONAL = "Proportional"
    const val EQUAL = "Equal"
    const val SKIP = "Skip"
    const val ADD_ITEM = "Add item"
    const val ADD_SHARED = "Add shared"
    const val ADD_CHARGE = "Add charge"
    const val SHARE_SPLIT = "Share split"
    const val DONE = "Done"

    // Share
    const val SHARE = "Share"
    const val SHARE_IMAGE = "Share image"
    const val PICK_PAYMENT = "Pick a payment method"
    const val PICK_PAYMENT_HINT = "Friends will use this to pay you back. It's embedded on the share image."
    const val ADD_FIRST_PAYMENT = "Add a payment method to share"
    const val ADD_METHOD = "Add method"

    // Settings
    const val SETTINGS = "Settings"
    const val PAYMENT_METHODS = "Payment methods"
    const val DEFAULT_CURRENCY = "Default currency"
    const val THEME = "Theme"
    const val LANGUAGE = "Language"
    const val PRIVACY = "Privacy"
    const val HELP_FEEDBACK = "Help & feedback"
    const val VERSION = "Version"
    const val APPEARANCE = "Appearance"
    const val ABOUT = "About"
    const val SPLITS = "Splits"

    // Generic
    const val BACK = "Back"
    const val CANCEL = "Cancel"
    const val SAVE = "Save"
    const val DELETE = "Delete"
    const val ADD = "Add"

    // List of every UI key, used by the Translator to batch-translate.
    val all: List<String> = listOf(
        GET_STARTED, NEW_SPLIT, SPLITS_THIS_MONTH, HISTORY,
        AUTO_DETECT, HOLD_STEADY, PHOTOS,
        WHO_GOT_WHAT, DESCRIBE_HINT, DESCRIBE_PLACEHOLDER, SPLIT_IT,
        SPLITTING_BILL, USUALLY_TAKES,
        REVIEW, PER_PERSON, SHARED, TAX_CHARGES, SPLIT_TAX_SERVICE,
        PROPORTIONAL, EQUAL, SKIP, ADD_ITEM, ADD_SHARED, ADD_CHARGE,
        SHARE_SPLIT, DONE,
        SHARE, SHARE_IMAGE, PICK_PAYMENT, PICK_PAYMENT_HINT,
        ADD_FIRST_PAYMENT, ADD_METHOD,
        SETTINGS, PAYMENT_METHODS, DEFAULT_CURRENCY, THEME, LANGUAGE,
        PRIVACY, HELP_FEEDBACK, VERSION, APPEARANCE, ABOUT, SPLITS,
        BACK, CANCEL, SAVE, DELETE, ADD,
    )
}
