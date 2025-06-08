#version 330 core

out vec4 FragColor;

uniform vec2 u_resolution;
uniform float u_zoom;
uniform vec2 u_offset;
uniform sampler2D u_texture; // Image

void main() {
    vec2 uv = (gl_FragCoord.xy / u_resolution) * 2.0 - 1.0;
    uv.x *= u_resolution.x / u_resolution.y;

    vec2 c = uv * u_zoom + u_offset;

    vec2 z = vec2(0.0);
    vec2 avg = vec2(0.0);

    int maxIter = 1000;
    int i = 0;
    for (; i < maxIter; i++) {
        z = vec2(z.x * z.x - z.y * z.y, 2.0 * z.x * z.y) + c;
        avg += z;

        if (dot(z, z) > 4.0) break;
    }

    if (i == maxIter) {
        FragColor = vec4(0.0); // inside set
    } else {
        vec2 mean = avg / float(i);

        // Map complex coordinate to texture UV [0,1]
        vec2 uvTex = (mean + vec2(2.0, 2.0)) / 4.0;

        // Sample color from texture
        vec3 color = texture(u_texture, uvTex).rgb;

        FragColor = vec4(color, 1.0);
    }
}
