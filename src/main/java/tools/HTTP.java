package tools;

enum HTTP {

    OK(200),
    NOT_FOUND(404),
    INTERNAL_SERVER_ERROR(500);
    int status;

    HTTP(int v) {
        this.status = v;
    }
}
