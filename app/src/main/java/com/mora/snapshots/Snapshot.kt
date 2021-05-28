package com.mora.snapshots

import com.google.firebase.database.Exclude

data class Snapshot(@get:Exclude var id: String = "",
                    var title:String = "",
                    var photo_url: String = "",
                    var like_list: Map<String, Boolean> = mutableMapOf())
