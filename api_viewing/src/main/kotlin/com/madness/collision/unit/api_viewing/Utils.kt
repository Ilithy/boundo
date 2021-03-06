package com.madness.collision.unit.api_viewing

import android.content.Context
import com.madness.collision.util.X
import com.madness.collision.util.X.A
import com.madness.collision.util.X.B
import com.madness.collision.util.X.C
import com.madness.collision.util.X.D
import com.madness.collision.util.X.E
import com.madness.collision.util.X.E_0_1
import com.madness.collision.util.X.E_MR1
import com.madness.collision.util.X.F
import com.madness.collision.util.X.G
import com.madness.collision.util.X.G_MR1
import com.madness.collision.util.X.H
import com.madness.collision.util.X.H_MR1
import com.madness.collision.util.X.H_MR2
import com.madness.collision.util.X.I
import com.madness.collision.util.X.I_MR1
import com.madness.collision.util.X.J
import com.madness.collision.util.X.J_MR1
import com.madness.collision.util.X.J_MR2
import com.madness.collision.util.X.K
import com.madness.collision.util.X.K_WATCH
import com.madness.collision.util.X.L
import com.madness.collision.util.X.L_MR1
import com.madness.collision.util.X.M
import com.madness.collision.util.X.N
import com.madness.collision.util.X.N_MR1
import com.madness.collision.util.X.O
import com.madness.collision.util.X.O_MR1
import com.madness.collision.util.X.P
import com.madness.collision.util.X.Q
import java.security.Principal
import java.util.regex.Matcher
import java.util.regex.Pattern

internal object Utils {


    fun getAndroidVersionByAPI(api: Int, exact: Boolean): String{
        return when (api){
            X.DEV -> if (exact) "DEV" else ""
            X.R -> "11"
            Q -> "10"
            P -> "9"  // 9 Pie
            O_MR1 -> if (exact) "8.1.0" else "8"  // 8.1.0 Oreo
            O -> if (exact) "8.0.0" else "8"  // 8.0.0 Oreo
            N_MR1-> if (exact) "7.1" else "7"  // 7.1 Nougat
            N -> if (exact) "7.0" else "7"  // 7.0 Nougat
            M -> if (exact) "6.0" else "6"  // 6.0 Marshmallow
            L_MR1 -> if (exact) "5.1" else "5"  // 5.1 Lollipop
            L -> if (exact) "5.0" else "5"  // 5.0 Lollipop
            K_WATCH -> if (exact) "4.4W" else "4"  // 4.4W KitKat
            K -> if (exact) "4.4 - 4.4.4" else "4"  // 4.4 - 4.4.4 KitKat
            J_MR2 -> if (exact) "4.3.x" else "4"  // 4.3.x Jelly Bean
            J_MR1 -> if (exact) "4.2.x" else "4"  // 4.2.x Jelly Bean
            J -> if (exact) "4.1.x" else "4"  // 4.1.x Jelly Bean
            I_MR1 -> if (exact) "4.0.3 - 4.0.4" else "4"  // 4.0.3 - 4.0.4 Ice Cream Sandwich
            I -> if (exact) "4.0.1 - 4.0.2" else "4"  // 4.0.1 - 4.0.2 Ice Cream Sandwich
            H_MR2 -> if (exact) "3.2.x" else "3"  // 3.2.x Honeycomb
            H_MR1 -> if (exact) "3.1" else "3"  // 3.1 Honeycomb
            H -> if (exact) "3.0" else "3"  // 3.0 Honeycomb
            G_MR1 -> if (exact)  "2.3.3 - 2.3.7" else "2"  // 2.3.3 - 2.3.7 Gingerbread
            G -> if (exact) "2.3 - 2.3.2" else "2"  // 2.3 - 2.3.2 Gingerbread
            F -> if (exact) "2.2.x" else "2"  // 2.2.x Froyo
            E_MR1 -> if (exact) "2.1" else "2"  // 2.1 Eclair
            E_0_1 -> if (exact) "2.0.1" else "2"  // 2.0.1 Eclair
            E -> if (exact) "2.0" else "2"  // 2.0 Eclair
            D -> if (exact) "1.6" else "1"  // 1.6 Donut
            C -> if (exact) "1.5" else "1"  // 1.5 Cupcake
            B -> if (exact) "1.1" else "1"  // 1.1 null
            A -> if (exact) "1.0" else "1"  // 1.0 null
            else -> ""
        }
    }

    fun getAndroidLetterByAPI(apiLevel: Int): Char{
        return androidCodenameInfo(null, apiLevel, false)[0]
    }

    fun getAndroidCodenameByAPI( context: Context, api: Int): String{
        return androidCodenameInfo(context, api, true)
    }

    private fun androidCodenameInfo(context: Context?, apiLevel: Int, fullName: Boolean): String {
        if (fullName) context ?: return " "
        return when (apiLevel) {
            X.DEV -> if (fullName) "DEV" else " "
            X.R -> if (fullName) "R" else "r"
            Q -> if (fullName) "Q" else "q"
            P -> if (fullName) context!!.getString(R.string.res_api_code_names_p) else "p"  // Pie
            O, O_MR1 -> if (fullName) context!!.getString(R.string.res_api_code_names_o) else "o"  // Oreo
            N, N_MR1 -> if (fullName) context!!.getString(R.string.res_api_code_names_n) else "n"  // Nougat
            M -> if (fullName) context!!.getString(R.string.res_api_code_names_m) else "m"  // Marshmallow
            L, L_MR1 -> if (fullName) context!!.getString(R.string.res_api_code_names_l) else "l"  // Lollipop
            K, K_WATCH -> if (fullName) context!!.getString(R.string.res_api_code_names_k) else "k"  // KitKat
            J, J_MR1, J_MR2 -> if (fullName) context!!.getString(R.string.res_api_code_names_j) else "j"  // Jelly Bean
            I, I_MR1 -> if (fullName) context!!.getString(R.string.res_api_code_names_i) else "i"  // Ice Cream Sandwich
            H, H_MR1, H_MR2 -> if (fullName) context!!.getString(R.string.res_api_code_names_h) else "h"  // Honeycomb
            G, G_MR1 -> if (fullName) context!!.getString(R.string.res_api_code_names_g) else "g"  // Gingerbread
            F -> if (fullName) context!!.getString(R.string.res_api_code_names_f) else "f"  // Froyo
            E, E_0_1, E_MR1 -> if (fullName) context!!.getString(R.string.res_api_code_names_e) else "e"  // Eclair
            D -> if (fullName) context!!.getString(R.string.res_api_code_names_d) else "d"  // Donut
            C -> if (fullName) context!!.getString(R.string.res_api_code_names_c) else "c"  // Cupcake
            B -> if (fullName) context!!.getString(R.string.resApiCodeNamesB) else " "  // from wikipedia
//            1 -> if (fullName) "Base" else " " // from Build.VERSION_CODES.BASE
            else -> if (fullName) "" else " "
        }
    }

    fun principalFields( context: Context, regexFields: MutableMap<String, String> ){
        regexFields["^(CN)(=.*)"] = context.getString(R.string.resPrincipalCN)
        regexFields["^(OU)(=.*)"] = context.getString(R.string.resPrincipalOU)
        regexFields["^(O)(=.*)"] = context.getString(R.string.resPrincipalO)
        regexFields["^(L)(=.*)"] = context.getString(R.string.resPrincipalL)
        regexFields["^(ST)(=.*)"] = context.getString(R.string.resPrincipalST)
        regexFields["^(C)(=.*)"] = context.getString(R.string.resPrincipalC)
        regexFields["^(EMAILADDRESS)(=.*)"] = context.getString(R.string.resPrincipalEmail)
    }

    fun getDesc(regexFields: Map<String, String> ,  p: Principal): String{
        val slices = p.toString().split(", ")
        val keys = regexFields.keys.toTypedArray()
        val builder = StringBuilder()
        var matcher : Matcher
        var matched = false
        for (s in slices){
            for (j in 0 until regexFields.size) {
                if (keys[j].isEmpty()) continue
                matcher = Pattern.compile(keys[j]).matcher(s)
                if (matcher.matches()){
                    matched = true
                    builder.append(matcher.group(1))
                            .append('(')
                            .append(regexFields[keys[j]])
                            .append(')')
                            .append(matcher.group(2))
                            .append(", ")
                    keys[j] = ""
                    break
                }
            }
            if (!matched) builder.append(s).append(", ")
            matched = false
        }
        return builder.delete(builder.length-2, builder.length-1).toString()
    }
}
