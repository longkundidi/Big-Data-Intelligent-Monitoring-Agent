export const SI_get = function (key: string) {
    if (!key) return '';

    return sessionStorage[key] || '';
}

export const SI_set = function (key: string, value: any) {
    if (!!key) {
        sessionStorage.setItem(key, value)
    }
}

export const SI_set_all = function (obj: any) {
    let keys = Object.keys(obj)
    keys.forEach(key => {
        SI_set(key, obj[key])
    })

}

