def call(def server, def port) {

    //sshagent(['RemoteCredentials']) {
        sh "echo ${server}:${port}"
    //}
}
